/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util.internal.net.rudp;

import net.rwhps.server.io.input.ClearableAndReusableByteArrayInputStream;
import net.rwhps.server.util.internal.net.rudp.impl.*;
import net.rwhps.server.util.log.Log;
import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * This class implements client sockets that use
 * the Simple Reliable UDP (Reliable UDP) protocol for
 * end-to-end communication between two machines.
 *
 * @author Adrian Granados
 * @author Dr (dr@der.kim)
 * @see java.net.Socket
 */
public class ReliableSocket extends Socket implements AbstractSelection {
    public ReliableSocket() {
        super();
    }

    public ReliableSocket(Proxy proxy) {
        super(proxy);
    }

    protected ReliableSocket(SocketImpl impl) throws SocketException {
        super(impl);
    }

    public ReliableSocket(String host, int port) throws IOException {
        super(host, port);
    }

    public ReliableSocket(InetAddress address, int port) throws IOException {
        super(address, port);
    }

    public ReliableSocket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
        super(host, port, localAddr, localPort);
    }

    public ReliableSocket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
        super(address, port, localAddr, localPort);
    }

    /**
     * Creates a Reliable UDP socket and attaches it to the underlying datagram socket.
     *
     * @param sock the datagram socket.
     */
    public ReliableSocket(DatagramSocket sock) {
        this(sock, new ReliableSocketProfile());
    }

    /**
     * Creates a Reliable UDP socket and attaches it to the underlying
     * datagram socket using the given Reliable UDP parameters.
     *
     * @param sock    the datagram socket.
     * @param profile the socket profile.
     */
    protected ReliableSocket(DatagramSocket sock, ReliableSocketProfile profile) {
        if (sock == null) {
            throw new NullPointerException("sock");
        }

        init(sock, profile);
    }

    /**
     * Initializes socket and sets it up for receiving incoming traffic.
     *
     * @param sock    the datagram socket.
     * @param profile the socket profile.
     */
    protected void init(DatagramSocket sock, ReliableSocketProfile profile) {
        _sock = sock;
        _profile = profile;
        _shutdownHook = new ShutdownHook();

        _sendBufferSize = (_profile.maxSegmentSize() - Segment.RUDP_HEADER_LEN) * 32;
        _recvBufferSize = (_profile.maxSegmentSize() - Segment.RUDP_HEADER_LEN) * 32;

        /* Register shutdown hook */
        try {
            Runtime.getRuntime().addShutdownHook(_shutdownHook);
        } catch (IllegalStateException xcp) {
            if (DEBUG) {
                Log.error("[Reliable UDP]", xcp);
            }
        }

        _sockThread.start();
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        _sock.bind(bindpoint);
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        connect(endpoint, 0);
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        if (endpoint == null) {
            throw new IllegalArgumentException("connect: The address can't be null");
        }

        if (timeout < 0) {
            throw new IllegalArgumentException("connect: timeout can't be negative");
        }

        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        if (isConnected()) {
            throw new SocketException("already connected");
        }

        if (!(endpoint instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("Unsupported address type");
        }

        this.endpoint = endpoint;

        // Synchronize sequence numbers
        _state = SYN_SENT;
        Random rand = new Random(System.currentTimeMillis());
        Segment syn = new SYNSegment(_counters.setSequenceNumber(rand.nextInt(MAX_SEQUENCE_NUMBER)),
                _profile.maxOutstandingSegs(),
                _profile.maxSegmentSize(),
                _profile.retransmissionTimeout(),
                _profile.cumulativeAckTimeout(),
                _profile.nullSegmentTimeout(),
                _profile.maxRetrans(),
                _profile.maxCumulativeAcks(),
                _profile.maxOutOfSequence(),
                _profile.maxAutoReset());

        sendAndQueueSegment(syn);

        // Wait for connection establishment (or timeout)
        boolean timedout = false;
        synchronized (this) {
            if (!isConnected()) {
                try {
                    if (timeout == 0) {
                        wait();
                    } else {
                        long startTime = System.currentTimeMillis();
                        wait(timeout);
                        if (System.currentTimeMillis() - startTime >= timeout) {
                            timedout = true;
                        }
                    }
                } catch (InterruptedException xcp) {
                    Log.error("[Reliable UDP]", xcp);
                }
            }
        }

        if (_state == ESTABLISHED) {
            return;
        }

        synchronized (_unackedSentQueue) {
            _unackedSentQueue.clear();
            _unackedSentQueue.notifyAll();
        }

        _counters.reset();
        _retransmissionTimer.cancel();

        switch (_state) {
            case SYN_SENT:
                connectionRefused();
                _state = CLOSED;
                if (timedout) {
                    throw new SocketTimeoutException();
                }
                throw new SocketException("Connection refused");
            case CLOSED:
            case CLOSE_WAIT:
                _state = CLOSED;
                throw new SocketException("Socket closed");
        }
    }

    @Override
    public InetAddress getInetAddress() {
        if (!isConnected()) {
            return null;
        }

        return ((InetSocketAddress) endpoint).getAddress();
    }

    @Override
    public int getPort() {
        if (!isConnected()) {
            return 0;
        }

        return ((InetSocketAddress) endpoint).getPort();

    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        if (!isConnected()) {
            return null;
        }

        return new InetSocketAddress(getInetAddress(), getPort());
    }

    @Override
    public InetAddress getLocalAddress() {
        return _sock.getLocalAddress();
    }

    @Override
    public int getLocalPort() {
        return _sock.getLocalPort();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return _sock.getLocalSocketAddress();
    }

    @Override
    public ClearableAndReusableByteArrayInputStream getInputStream() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        if (!isConnected()) {
            throw new SocketException("Socket is not connected");
        }

        if (isInputShutdown()) {
            throw new SocketException("Socket input is shutdown");
        }

        return _in;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        if (!isConnected()) {
            throw new SocketException("Socket is not connected");
        }

        if (isOutputShutdown()) {
            throw new SocketException("Socket output is shutdown");
        }

        return _out;
    }

    @Override
    public synchronized void close() throws IOException {
        synchronized (_closeLock) {

            if (isClosed()) {
                return;
            }

            try {
                Runtime.getRuntime().removeShutdownHook(_shutdownHook);
            } catch (IllegalStateException xcp) {
                if (DEBUG) {
                    Log.error("[Reliable UDP]", xcp);
                }
            }

            switch (_state) {
                case SYN_SENT:
                    synchronized (this) {
                        notify();
                    }
                    break;
                case CLOSE_WAIT:
                case SYN_RCVD:
                case ESTABLISHED:
                    sendSegment(new FINSegment(_counters.nextSequenceNumber()));
                    closeImpl();
                    break;
                case CLOSED:
                    _retransmissionTimer.destroy();
                    _cumulativeAckTimer.destroy();
                    _keepAliveTimer.destroy();
                    _nullSegmentTimer.destroy();
                    _sock.close();
                    break;
            }

            _closed = true;
            _state = CLOSED;

            synchronized (_unackedSentQueue) {
                _unackedSentQueue.notify();
            }

            synchronized (_inSeqRecvQueue) {
                _inSeqRecvQueue.notify();
            }
        }
    }

    @Override
    public boolean isBound() {
        return _sock.isBound();
    }

    @Override
    public boolean isConnected() {
        return _connected;
    }

    @Override
    public boolean isClosed() {
        synchronized (_closeLock) {
            return _closed;
        }
    }

    @Override
    public synchronized void setSendBufferSize(int size)
            throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("negative receive size");
        }

        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        if (isConnected()) {
            return;
        }

        _sendBufferSize = size;
    }

    @Override
    public synchronized int getSendBufferSize()
            throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        return _sendBufferSize;
    }

    @Override
    public synchronized void setReceiveBufferSize(int size)
            throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("negative send size");
        }

        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        if (isConnected()) {
            return;
        }

        _recvBufferSize = size;
    }

    @Override
    public synchronized int getReceiveBufferSize() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        return _recvBufferSize;
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        throw new SocketException("Socket option not supported");
    }

    @Override
    public boolean getTcpNoDelay() {
        return false;
    }

    @Override
    public synchronized void setKeepAlive(boolean on)
            throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        if (_keepAlive == on) {
            return;
        }

        _keepAlive = on;

        if (isConnected()) {
            if (_keepAlive) {
                _keepAliveTimer.schedule(_profile.nullSegmentTimeout() * 6L, _profile.nullSegmentTimeout() * 6L);
            } else {
                _keepAliveTimer.cancel();
            }
        }
    }

    @Override
    public synchronized boolean getKeepAlive()
            throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        return _keepAlive;
    }

    @Override
    public void shutdownInput() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        if (!isConnected()) {
            throw new SocketException("Socket is not connected");
        }

        if (isInputShutdown()) {
            throw new SocketException("Socket input is already shutdown");
        }

        _shutIn = true;

        synchronized (_recvQueueLock) {
            _recvQueueLock.notify();
        }
    }

    @Override
    public void shutdownOutput() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        if (!isConnected()) {
            throw new SocketException("Socket is not connected");
        }

        if (isOutputShutdown()) {
            throw new SocketException("Socket output is already shutdown");
        }

        _shutOut = true;

        synchronized (_unackedSentQueue) {
            _unackedSentQueue.notifyAll();
        }
    }

    @Override
    public boolean isInputShutdown() {
        return _shutIn;
    }

    @Override
    public boolean isOutputShutdown() {
        return _shutOut;
    }

    /**
     * Resets the socket state.
     * <p>
     * The socket will attempt to deliver all outstanding bytes to the remote
     * endpoint and then it will renegotiate the connection parameters.
     * The transmissions of bytes resumes after the renegotation finishes and
     * the connection is synchronized again.
     *
     * @throws IOException if an I/O error occurs when resetting the connection.
     */
    public void reset() throws IOException {
        reset(null);
    }

    /**
     * Resets the socket state and profile.
     * <p>
     * The socket will attempt to deliver all outstanding bytes to the remote
     * endpoint and then it will renegotiate the connection parameters
     * specified in the given socket profile.
     * The transmissions of bytes resumes after the renegotation finishes and
     * the connection is synchronized again.
     *
     * @param profile the socket profile or null if old profile should be used.
     * @throws IOException if an I/O error occurs when resetting the connection.
     */
    public void reset(ReliableSocketProfile profile) throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        if (!isConnected()) {
            throw new SocketException("Socket is not connected");
        }

        synchronized (_resetLock) {
            _reset = true;

            sendAndQueueSegment(new RSTSegment(_counters.nextSequenceNumber()));

            // Wait to flush all outstanding segments (including last RST segment).
            synchronized (_unackedSentQueue) {
                while (!_unackedSentQueue.isEmpty()) {
                    try {
                        _unackedSentQueue.wait();
                    } catch (InterruptedException xcp) {
                        Log.error("[Reliable UDP]", xcp);
                    }
                }
            }
        }

        connectionReset();

        // Set new profile
        if (profile != null) {
            _profile = profile;
        }

        // Synchronize sequence numbers
        _state = SYN_SENT;
        Random rand = new Random(System.currentTimeMillis());
        Segment syn = new SYNSegment(_counters.setSequenceNumber(rand.nextInt(MAX_SEQUENCE_NUMBER)),
                _profile.maxOutstandingSegs(),
                _profile.maxSegmentSize(),
                _profile.retransmissionTimeout(),
                _profile.cumulativeAckTimeout(),
                _profile.nullSegmentTimeout(),
                _profile.maxRetrans(),
                _profile.maxCumulativeAcks(),
                _profile.maxOutOfSequence(),
                _profile.maxAutoReset());

        sendAndQueueSegment(syn);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> as data segments and
     * queues them for immediate transmission.
     *
     * @param b   the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException if an I/O error occurs. In particular,
     *                     an <code>IOException</code> is thrown if the socket
     *                     is closed.
     */
    protected void write(byte[] b, int off, int len) throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        if (isOutputShutdown()) {
            throw new IOException("Socket output is shutdown");
        }

        if (!isConnected()) {
            throw new SocketException("Connection reset");
        }

        int totalBytes = 0;
        while (totalBytes < len) {
            synchronized (_resetLock) {
                while (_reset) {
                    try {
                        _resetLock.wait();
                    } catch (InterruptedException xcp) {
                        Log.error("[Reliable UDP]", xcp);
                    }
                }

                int writeBytes = Math.min(_profile.maxSegmentSize() - Segment.RUDP_HEADER_LEN, len - totalBytes);

                sendAndQueueSegment(new DATSegment(_counters.nextSequenceNumber(), _counters.getLastInSequence(), b, off + totalBytes, writeBytes));
                totalBytes += writeBytes;
            }
        }
    }

    /**
     * Reads up to <code>len</code> bytes of data from the receiver
     * buffer into an array of bytes.  An attempt is made to read
     * as many as <code>len</code> bytes, but a smaller number may
     * be read. The number of bytes actually read is returned as
     * an integer.
     * <p>
     * This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset in array <code>b</code>
     *            at which the data is written.
     * @param len the maximum number of bytes to read.
     * @return the total number of bytes read into the buffer,
     * or <code>-1</code> if there is no more data because
     * the end of the stream has been reached.
     * @throws IOException if an I/O error occurs. In particular,
     *                     an <code>IOException</code> is thrown if the socket
     *                     is closed, or if the buffer is not big enough to hold
     *                     a full data segment.
     */
    protected int read(byte[] b, int off, int len) throws IOException {

        int totalBytes = 0;

        /*
         * 我们通过魔改 Socket.read
         * 来实现了, 有 Packet 时就刷新一次 read, 尝试读取数据, 然后扔进解码器
         * 这样就可以在以后实现一个虚假的 NIO
         */
        synchronized (_recvQueueLock) {
            if (_inSeqRecvQueue.isEmpty()) {
                return 0;
            }

            if (isClosed()) {
                throw new SocketException("Socket is closed");
            }

            if (isInputShutdown()) {
                throw new EOFException();
            }

            if (!isConnected()) {
                throw new SocketException("Connection reset");
            }

            for (Iterator<Segment> it = _inSeqRecvQueue.iterator(); it.hasNext(); ) {
                Segment s = it.next();

                if (s instanceof RSTSegment) {
                    it.remove();
                    break;
                } else if (s instanceof FINSegment) {
                    if (totalBytes <= 0) {
                        it.remove();
                        /* EOF */
                        return -1;
                    }
                    break;
                } else if (s instanceof DATSegment) {
                    byte[] data = ((DATSegment) s).getData();
                    if (data.length + totalBytes > len) {
                        if (totalBytes <= 0) {
                            throw new IOException("insufficient buffer space");
                        }
                        break;
                    }

                    System.arraycopy(data, 0, b, off + totalBytes, data.length);
                    totalBytes += data.length;
                    it.remove();
                }
            }

            return totalBytes;
        }
    }

    /**
     * Adds the specified state listener to this socket. If the listener
     * has already been registered, this method does nothing.
     *
     * @param stateListener the listener to add.
     */
    public void addStateListener(ReliableSocketStateListener stateListener) {
        if (stateListener == null) {
            throw new NullPointerException("stateListener");
        }

        synchronized (_stateListeners) {
            if (!_stateListeners.contains(stateListener)) {
                _stateListeners.add(stateListener);
            }
        }
    }

    /**
     * Removes the specified state listener from this socket. This is
     * harmless if the listener was not previously registered.
     *
     * @param stateListener the listener to remove.
     */
    public void removeStateListener(ReliableSocketStateListener stateListener) {
        if (stateListener == null) {
            throw new NullPointerException("stateListener");
        }

        synchronized (_stateListeners) {
            _stateListeners.remove(stateListener);
        }
    }

    /**
     * Sends a segment piggy-backing any pending acknowledgments.
     *
     * @param s the segment.
     * @throws IOException if an I/O error occurs in the
     *                     underlying UDP socket.
     */
    private void sendSegment(Segment s) throws IOException {
        /* Piggyback any pending acknowledgments */
        if (s instanceof DATSegment || s instanceof RSTSegment || s instanceof FINSegment || s instanceof NULSegment) {
            checkAndSetAck(s);
        }

        /* Reset null segment timer */
        if (s instanceof DATSegment || s instanceof RSTSegment || s instanceof FINSegment) {
            _nullSegmentTimer.reset();
        }

        if (DEBUG) {
            log("sent " + s);
        }

        sendSegmentImpl(s);
    }

    /**
     * Receives a segment and increases the cumulative
     * acknowledgment counter.
     *
     * @return the received segment.
     * @throws IOException if an I/O error occurs in the
     *                     underlying UDP socket.
     */
    private Segment receiveSegment() throws IOException {
        Segment s;
        if ((s = receiveSegmentImpl()) != null) {

            if (DEBUG) {
                log("recv " + s);
            }

            if (s instanceof DATSegment || s instanceof NULSegment ||
                    s instanceof RSTSegment || s instanceof FINSegment ||
                    s instanceof SYNSegment) {
                _counters.incCumulativeAckCounter();
            }

            if (_keepAlive) {
                _keepAliveTimer.reset();
            }
        }

        return s;
    }

    /**
     * Sends a segment and queues a copy of it in the queue of unacknowledged segments.
     *
     * @param segment a segment for which delivery must be guaranteed.
     * @throws IOException if an I/O error occurs in the
     *                     underlying UDP socket.
     */
    private void sendAndQueueSegment(Segment segment) throws IOException {
        synchronized (_unackedSentQueue) {
            while ((_unackedSentQueue.size() >= _sendQueueSize) ||
                    (_counters.getOutstandingSegsCounter() > _profile.maxOutstandingSegs())) {
                if (!_connected) {
                    throw new SocketException("Socket is closed");
                }
                try {
                    _unackedSentQueue.wait();
                } catch (InterruptedException xcp) {
                    Log.error("[Reliable UDP]", xcp);
                }
            }

            _counters.incOutstandingSegsCounter();
            _unackedSentQueue.add(segment);
        }

        if (_closed) {
            throw new SocketException("Socket is closed");
        }

        /* Re-start retransmission timer */
        if (!(segment instanceof ACKSegment)) {
            synchronized (_retransmissionTimer) {
                if (_retransmissionTimer.isIdle()) {
                    _retransmissionTimer.schedule(_profile.retransmissionTimeout(), _profile.retransmissionTimeout());
                }
            }
        }

        sendSegment(segment);
    }

    /**
     * Sends a segment and increments its retransmission counter.
     *
     * @param segment the segment to be retransmitted.
     * @throws IOException if an I/O error occurs in the
     *                     underlying UDP socket.
     */
    private void retransmitSegment(Segment segment) throws IOException {
        if (_profile.maxRetrans() > 0) {
            segment.setRetxCounter(segment.getRetxCounter() + 1);
        }

        if (_profile.maxRetrans() != 0 && segment.getRetxCounter() > _profile.maxRetrans()) {
            connectionFailure();
            return;
        }

        sendSegment(segment);
    }

    /**
     * Puts the connection in an "opened" state and notifies all
     * registered state listeners that the connection is opened.
     */
    private void connectionOpened() {
        if (isConnected()) {

            _nullSegmentTimer.cancel();

            if (_keepAlive) {
                _keepAliveTimer.cancel();
            }

            synchronized (_resetLock) {
                _reset = false;
                _resetLock.notify();
            }
        } else {
            synchronized (this) {
                _in = new ReliableSocketInputStream(this);
                _out = new ReliableSocketOutputStream(this);
                _connected = true;
                _state = ESTABLISHED;

                notify();
            }

            synchronized (_stateListeners) {
                for (ReliableSocketStateListener l : _stateListeners) {
                    l.connectionOpened(this);
                }
            }
        }

        _nullSegmentTimer.schedule(0, _profile.nullSegmentTimeout());

        if (_keepAlive) {
            _keepAliveTimer.schedule(_profile.nullSegmentTimeout() * 6L, _profile.nullSegmentTimeout() * 6L);
        }
    }

    /**
     * Notifies all registered state listeners that
     * the connection attempt has been refused.
     */
    private void connectionRefused() {
        synchronized (_stateListeners) {
            for (ReliableSocketStateListener l : _stateListeners) {
                l.connectionRefused(this);
            }
        }
    }

    /**
     * Notifies all registered state listeners
     * that the connection has been closed.
     */
    private void connectionClosed() {
        synchronized (_stateListeners) {
            for (ReliableSocketStateListener l : _stateListeners) {
                l.connectionClosed(this);
            }
        }
    }

    /**
     * Puts the connection in a closed state and notifies all
     * registered state listeners that the connection failed.
     */
    private void connectionFailure() {
        synchronized (_closeLock) {

            if (isClosed()) {
                return;
            }

            switch (_state) {
                case SYN_SENT:
                    synchronized (this) {
                        notify();
                    }
                    break;
                case CLOSE_WAIT:
                case SYN_RCVD:
                case ESTABLISHED:
                    _connected = false;
                    synchronized (_unackedSentQueue) {
                        _unackedSentQueue.notifyAll();
                    }

                    synchronized (_recvQueueLock) {
                        _recvQueueLock.notify();
                    }

                    closeImpl();
                    break;
            }

            _state = CLOSED;
            _closed = true;
        }

        synchronized (_stateListeners) {
            for (ReliableSocketStateListener l : _stateListeners) {
                l.connectionFailure(this);
            }
        }
    }

    /**
     * Notifies all registered state listeners
     * that the connection has been reset.
     */
    private void connectionReset() {
        synchronized (_stateListeners) {
            for (ReliableSocketStateListener l : _stateListeners) {
                l.connectionReset(this);
            }
        }
    }

    /**
     * Handles a received SYN segment.
     * <p>
     * When a client initiates a connection it sends a SYN segment which
     * contains the negotiable parameters defined by the Upper Layer Protocol
     * via the API. The server can accept these parameters by echoing them back
     * in its SYN with ACK response or propose different parameters in its SYN
     * with ACK response. The client can then choose to accept the parameters
     * sent by the server by sending an ACK to establish the connection or it can
     * refuse the connection by sending a FIN.
     *
     * @param segment the SYN segment.
     */
    private void handleSYNSegment(SYNSegment segment) {
        try {
            switch (_state) {
                case CLOSED:
                    _counters.setLastInSequence(segment.seq());
                    _state = SYN_RCVD;

                    Random rand = new Random(System.currentTimeMillis());
                    _profile = new ReliableSocketProfile(
                            _sendQueueSize,
                            _recvQueueSize,
                            segment.getMaxSegmentSize(),
                            segment.getMaxOutstandingSegments(),
                            segment.getMaxRetransmissions(),
                            segment.getMaxCumulativeAcks(),
                            segment.getMaxOutOfSequence(),
                            segment.getMaxAutoReset(),
                            segment.getNulSegmentTimeout(),
                            segment.getRetransmissionTimeout(),
                            segment.getCummulativeAckTimeout());

                    Segment syn = new SYNSegment(_counters.setSequenceNumber(rand.nextInt(MAX_SEQUENCE_NUMBER)),
                            _profile.maxOutstandingSegs(),
                            _profile.maxSegmentSize(),
                            _profile.retransmissionTimeout(),
                            _profile.cumulativeAckTimeout(),
                            _profile.nullSegmentTimeout(),
                            _profile.maxRetrans(),
                            _profile.maxCumulativeAcks(),
                            _profile.maxOutOfSequence(),
                            _profile.maxAutoReset());

                    syn.setAck(segment.seq());
                    sendAndQueueSegment(syn);
                    break;
                case SYN_SENT:
                    _counters.setLastInSequence(segment.seq());
                    _state = ESTABLISHED;
                    /*
                     * Here the client accepts or rejects the parameters sent by the
                     * server. For now we will accept them.
                     */
                    sendAck();
                    connectionOpened();
                    break;
            }
        } catch (IOException xcp) {
            Log.error("[Reliable UDP]", xcp);
        }
    }

    /**
     * Handles a received EAK segment.
     * <p>
     * When a EAK segment is received, the segments specified in
     * the message are removed from the unacknowledged sent queue.
     * The segments to be retransmitted are determined by examining
     * the Ack Number and the last out of sequence ack number in the
     * EAK segment. All segments between but not including these two
     * sequence numbers that are on the unacknowledged sent queue are
     * retransmitted.
     *
     * @param segment the EAK segment.
     */
    private void handleEAKSegment(EAKSegment segment) {
        Iterator<Segment> it;
        int[] acks = segment.getACKs();

        int lastInSequence = segment.getAck();
        int lastOutSequence = acks[acks.length - 1];

        synchronized (_unackedSentQueue) {

            /* Removed acknowledged segments from sent queue */
            for (it = _unackedSentQueue.iterator(); it.hasNext(); ) {
                Segment s = it.next();
                if ((compareSequenceNumbers(s.seq(), lastInSequence) <= 0)) {
                    it.remove();
                    continue;
                }

                for (int ack : acks) {
                    if ((compareSequenceNumbers(s.seq(), ack) == 0)) {
                        it.remove();
                        break;
                    }
                }
            }

            /* Retransmit segments */
            it = _unackedSentQueue.iterator();
            while (it.hasNext()) {
                Segment s = it.next();
                if ((compareSequenceNumbers(lastInSequence, s.seq()) < 0) &&
                        (compareSequenceNumbers(lastOutSequence, s.seq()) > 0)) {

                    try {
                        retransmitSegment(s);
                    } catch (IOException xcp) {
                        Log.error("[Reliable UDP]", xcp);
                    }
                }
            }

            _unackedSentQueue.notifyAll();
        }
    }

    /**
     * Handles a received RST, FIN, or DAT segment.
     *
     * @param segment 数据包
     */
    public void handleSegment(@NotNull Segment segment) {
        /*
         * When a RST segment is received, the sender must stop
         * sending new packets, but most continue to attempt
         * delivery of packets already accepted from the application.
         */
        if (segment instanceof RSTSegment) {
            synchronized (_resetLock) {
                _reset = true;
            }

            connectionReset();
        }

        /*
         * When a FIN segment is received, no more packets
         * are expected to arrive after this segment.
         */
        if (segment instanceof FINSegment) {
            switch (_state) {
                case SYN_SENT:
                    synchronized (this) {
                        notify();
                    }
                    break;
                case CLOSED:
                    break;
                default:
                    _state = CLOSE_WAIT;
            }
        }

        boolean inSequence = false;
        synchronized (_recvQueueLock) {
            if (compareSequenceNumbers(segment.seq(), _counters.getLastInSequence()) <= 0) {
                /* Drop packet: duplicate. */
            } else if (compareSequenceNumbers(segment.seq(), nextSequenceNumber(_counters.getLastInSequence())) == 0) {
                inSequence = true;
                if (_inSeqRecvQueue.isEmpty() || (_inSeqRecvQueue.size() + _outSeqRecvQueue.size() < _recvQueueSize)) {
                    /* Insert in-sequence segment */
                    _counters.setLastInSequence(segment.seq());
                    if (segment instanceof DATSegment || segment instanceof RSTSegment || segment instanceof FINSegment) {
                        _inSeqRecvQueue.add(segment);
                    }

                    checkRecvQueues();
                } else {
                    /* Drop packet: queue is full. */
                }
            } else if (_inSeqRecvQueue.size() + _outSeqRecvQueue.size() < _recvQueueSize) {
                /* Insert out-of-sequence segment, in order */
                boolean added = false;
                for (int i = 0; i < _outSeqRecvQueue.size() && !added; i++) {
                    Segment s = _outSeqRecvQueue.get(i);
                    int cmp = compareSequenceNumbers(segment.seq(), s.seq());
                    if (cmp == 0) {
                        /* Ignore duplicate packet */
                        added = true;
                    } else if (cmp < 0) {
                        _outSeqRecvQueue.add(i, segment);
                        added = true;
                    }
                }

                if (!added) {
                    _outSeqRecvQueue.add(segment);
                }

                _counters.incOutOfSequenceCounter();
            }

            if (inSequence && (
                    segment instanceof RSTSegment || segment instanceof NULSegment || segment instanceof FINSegment
            )) {
                sendAck();
            } else if ((_counters.getOutOfSequenceCounter() > 0) &&
                    (_profile.maxOutOfSequence() == 0 || _counters.getOutOfSequenceCounter() > _profile.maxOutOfSequence())) {
                sendExtendedAck();
            } else if ((_counters.getCumulativeAckCounter() > 0) &&
                    (_profile.maxCumulativeAcks() == 0 || _counters.getCumulativeAckCounter() > _profile.maxCumulativeAcks())) {
                sendSingleAck();
            } else {
                synchronized (_cumulativeAckTimer) {
                    if (_cumulativeAckTimer.isIdle()) {
                        _cumulativeAckTimer.schedule(_profile.cumulativeAckTimeout());
                    }
                }
            }
        }
    }

    /**
     * Acknowledges the next segment to be acknowledged.
     * If there are any out-of-sequence segments in the
     * receiver queue, it sends an EAK segment.
     */
    private void sendAck() {
        synchronized (_recvQueueLock) {
            if (!_outSeqRecvQueue.isEmpty()) {
                sendExtendedAck();
                return;
            }

            sendSingleAck();
        }
    }

    /**
     * Sends an EAK segment if there is at least one
     * out-of-sequence received segment.
     */
    private void sendExtendedAck() {
        synchronized (_recvQueueLock) {

            if (_outSeqRecvQueue.isEmpty()) {
                return;
            }

            _counters.getAndResetCumulativeAckCounter();
            _counters.getAndResetOutOfSequenceCounter();

            /* Compose list of out-of-sequence sequence numbers */
            int[] acks = new int[_outSeqRecvQueue.size()];
            for (int i = 0; i < acks.length; i++) {
                Segment s = _outSeqRecvQueue.get(i);
                acks[i] = s.seq();
            }

            try {
                int lastInSequence = _counters.getLastInSequence();
                sendSegment(new EAKSegment(nextSequenceNumber(lastInSequence),
                        lastInSequence, acks));
            } catch (IOException xcp) {
                Log.error("[Reliable UDP]", xcp);
            }

        }
    }

    /**
     * Sends an ACK segment if there is a received segment to
     * be acknowledged.
     */
    private void sendSingleAck() {
        if (_counters.getAndResetCumulativeAckCounter() == 0) {
            return;
        }

        try {
            int lastInSequence = _counters.getLastInSequence();
            sendSegment(new ACKSegment(nextSequenceNumber(lastInSequence), lastInSequence));
        } catch (IOException xcp) {
            Log.error("[Reliable UDP]", xcp);
        }
    }

    /**
     * Sets the ACK flag and number of a segment if there is at least
     * one received segment to be acknowledged.
     *
     * @param s the segment.
     */
    private void checkAndSetAck(Segment s) {
        if (_counters.getAndResetCumulativeAckCounter() == 0) {
            return;
        }

        s.setAck(_counters.getLastInSequence());
    }

    /**
     * Checks the ACK flag and number of a segment.
     *
     * @param segment the segment.
     */
    private void checkAndGetAck(Segment segment) {
        int ackn = segment.getAck();

        if (ackn < 0) {
            return;
        }

        _counters.getAndResetOutstandingSegsCounter();

        if (_state == SYN_RCVD) {
            _state = ESTABLISHED;
            connectionOpened();
        }

        synchronized (_unackedSentQueue) {
            _unackedSentQueue.removeIf(s -> compareSequenceNumbers(s.seq(), ackn) <= 0);

            if (_unackedSentQueue.isEmpty()) {
                _retransmissionTimer.cancel();
            }

            _unackedSentQueue.notifyAll();
        }
    }

    /**
     * Checks for in-sequence segments in the out-of-sequence queue
     * that can be moved to the in-sequence queue.
     */
    private void checkRecvQueues() {
        synchronized (_recvQueueLock) {
            Iterator<Segment> it = _outSeqRecvQueue.iterator();
            while (it.hasNext()) {
                Segment s = it.next();
                if (compareSequenceNumbers(s.seq(), nextSequenceNumber(_counters.getLastInSequence())) == 0) {
                    _counters.setLastInSequence(s.seq());
                    if (s instanceof DATSegment || s instanceof RSTSegment || s instanceof FINSegment) {
                        _inSeqRecvQueue.add(s);
                    }
                    it.remove();
                }
            }

            _recvQueueLock.notify();
        }
    }

    /**
     * Writes out a segment to the underlying UDP socket.
     *
     * @param s the segment.
     */
    protected void sendSegmentImpl(Segment s) {
        try {
            DatagramPacket packet = new DatagramPacket(s.getBytes(), s.length(), endpoint);
            _sock.send(packet);
        } catch (IOException xcp) {
            if (!isClosed()) {
                Log.error("[Reliable UDP]", xcp);
            }
        }
    }

    /**
     * Reads in a segment from the underlying UDP socket.
     *
     * @return s    the segment.
     */
    protected Segment receiveSegmentImpl() {
        try {
            DatagramPacket packet = new DatagramPacket(_recvbuffer, _recvbuffer.length);
            _sock.receive(packet);
            return Segment.parse(packet.getData(), 0, packet.getLength());
        } catch (IOException ioXcp) {
            if (!isClosed()) {
                Log.error("[Reliable UDP]", ioXcp);
            }
        }

        return null;
    }

    /**
     * Closes the underlying UDP socket.
     */
    protected void closeSocket() {
        _sock.close();
    }

    /**
     * Cleans up and closes the socket.
     */
    protected void closeImpl() {
        _nullSegmentTimer.cancel();
        _keepAliveTimer.cancel();
        _state = CLOSE_WAIT;

        Thread t = new Thread(() -> {
            _keepAliveTimer.destroy();
            _nullSegmentTimer.destroy();

            try {
                Thread.sleep(_profile.nullSegmentTimeout() * 2L);
            } catch (InterruptedException xcp) {
                Log.error("[Reliable UDP]", xcp);
            }

            _retransmissionTimer.destroy();
            _cumulativeAckTimer.destroy();

            closeSocket();
            connectionClosed();
        });
        t.setName("ReliableSocket-Closing");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Log routine.
     */
    protected void log(String msg) {
        System.out.println(getLocalPort() + ": " + msg);
    }

    /**
     * Computes the consecutive sequence number.
     *
     * @return the next number in the sequence.
     */
    static int nextSequenceNumber(int seqn) {
        return (seqn + 1) % MAX_SEQUENCE_NUMBER;
    }

    /**
     * Compares two sequence numbers.
     *
     * @return 0, 1 or -1 if the first sequence number is equal,
     * greater or less than the second sequence number.
     * (see RFC 1982).
     */
    private int compareSequenceNumbers(int seqn, int aseqn) {
        if (seqn == aseqn) {
            return 0;
        } else if (((seqn < aseqn) && ((aseqn - seqn) > MAX_SEQUENCE_NUMBER / 2)) ||
                ((seqn > aseqn) && ((seqn - aseqn) < MAX_SEQUENCE_NUMBER / 2))) {
            return 1;
        } else {
            return -1;
        }
    }

    protected DatagramSocket _sock;
    protected SocketAddress endpoint;
    protected ReliableSocketInputStream _in;
    protected ReliableSocketOutputStream _out;

    private final byte[] _recvbuffer = new byte[65535];

    private boolean _closed = false;
    private boolean _connected = false;
    private boolean _reset = false;
    private boolean _keepAlive = true;
    private int _state = CLOSED;
    private boolean _shutIn = false;
    private boolean _shutOut = false;

    private final Object _closeLock = new Object();
    private final Object _resetLock = new Object();

    private final ArrayList<ReliableSocketStateListener> _stateListeners = new ArrayList<>();

    private ShutdownHook _shutdownHook;

    /* Reliable UDP connection parameters */
    private ReliableSocketProfile _profile = new ReliableSocketProfile();

    private final ArrayList<Segment> _unackedSentQueue = new ArrayList<>(); /* Unacknowledged segments send queue */
    private final ArrayList<Segment> _outSeqRecvQueue = new ArrayList<>(); /* Out-of-sequence received segments queue */
    private final ArrayList<Segment> _inSeqRecvQueue = new ArrayList<>(); /* In-sequence received segments queue */

    private final Object _recvQueueLock = new Object();  /* Lock for receiver queues */
    private final Counters _counters = new Counters(); /* Sequence number, ack counters, etc. */

    private final Thread _sockThread = new ReliableSocketThread();

    private final int _sendQueueSize = 32; /* Maximum number of received segments */
    private final int _recvQueueSize = 32; /* Maximum number of sent segments */

    private int _sendBufferSize;
    private int _recvBufferSize;

    /*
     * This timer is started when the connection is opened and is reset
     * every time a data segment is sent. If the client's null segment
     * timer expires, the client sends a null segment to the server.
     */
    private final Timer _nullSegmentTimer = new Timer("ReliableSocket-NullSegmentTimer", new NullSegmentTimerTask());

    /*
     * This timer is re-started every time a data, null, or reset
     * segment is sent and there is not a segment currently being timed.
     * If an acknowledgment for this data segment is not received by
     * the time the timer expires, all segments that have been sent but
     * not acknowledged are retransmitted. The Retransmission timer is
     * re-started when the timed segment is received, if there is still
     * one or more packets that have been sent but not acknowledged.
     */
    private final Timer _retransmissionTimer = new Timer("ReliableSocket-RetransmissionTimer", new RetransmissionTimerTask());

    /*
     * When this timer expires, if there are segments on the out-of-sequence
     * queue, an extended acknowledgment is sent. Otherwise, if there are
     * any segments currently unacknowledged, a stand-alone acknowledgment
     * is sent.
     * The cumulative acknowledge timer is restarted whenever an acknowledgment
     * is sent in a data, null, or reset segment, provided that there are no
     * segments currently on the out-of-sequence queue. If there are segments
     * on the out-of-sequence queue, the timer is not restarted, so that another
     * extended acknowledgment will be sent when it expires again.
     */
    private final Timer _cumulativeAckTimer = new Timer("ReliableSocket-CumulativeAckTimer", new CumulativeAckTimerTask());

    /*
     * When this timer expires, the connection is considered broken.
     */
    private final Timer _keepAliveTimer = new Timer("ReliableSocket-KeepAliveTimer", new KeepAliveTimerTask());

    private static final int MAX_SEQUENCE_NUMBER = 255;

    private static final int CLOSED = 0; /* There is not an active or pending connection */
    private static final int SYN_RCVD = 1; /* Request to connect received, waiting ACK */
    private static final int SYN_SENT = 2; /* Request to connect sent */
    private static final int ESTABLISHED = 3; /* Data transfer state */
    private static final int CLOSE_WAIT = 4; /* Request to close the connection */

    private static final boolean DEBUG = Boolean.getBoolean("net.rudp.debug");

    /*
     * -----------------------------------------------------------------------
     * INTERNAL CLASSES
     * -----------------------------------------------------------------------
     */

    private class ReliableSocketThread extends Thread {
        public ReliableSocketThread() {
            super("ReliableSocket");
            setDaemon(true);
        }

        @Override
        public void run() {
            Segment segment;
            try {
                while ((segment = receiveSegment()) != null) {

                    if (segment instanceof SYNSegment) {
                        handleSYNSegment((SYNSegment) segment);
                    } else if (segment instanceof EAKSegment) {
                        handleEAKSegment((EAKSegment) segment);
                    } else if (segment instanceof ACKSegment) {
                        // do nothing.
                    } else {
                        handleSegment(segment);
                    }

                    checkAndGetAck(segment);
                }
            } catch (IOException xcp) {
                Log.error("[Reliable UDP]", xcp);
            }
        }
    }

    private class NullSegmentTimerTask implements Runnable {
        public void run() {
            // Send a new NULL segment if there is nothing to be retransmitted.
            synchronized (_unackedSentQueue) {
                if (_unackedSentQueue.isEmpty()) {
                    try {
                        sendAndQueueSegment(new NULSegment(_counters.nextSequenceNumber()));
                    } catch (IOException xcp) {
                        if (DEBUG) {
                            Log.error("[Reliable UDP]", xcp);
                        }
                    }
                }
            }
        }
    }

    private class RetransmissionTimerTask implements Runnable {
        public void run() {
            synchronized (_unackedSentQueue) {
                for (Segment s : _unackedSentQueue) {
                    try {
                        retransmitSegment(s);
                    } catch (IOException xcp) {
                        Log.error("[Reliable UDP]", xcp);
                    }
                }
            }
        }
    }

    private class CumulativeAckTimerTask implements Runnable {
        public void run() {
            sendAck();
        }
    }

    private class KeepAliveTimerTask implements Runnable {
        public void run() {
            connectionFailure();
        }
    }

    private class ShutdownHook extends Thread {
        public ShutdownHook() {
            super("ReliableSocket-ShutdownHook");
        }

        @Override
        public void run() {
            try {
                if (_state == CLOSED) {
                } else {
                    sendSegment(new FINSegment(_counters.nextSequenceNumber()));
                }
            } catch (Throwable t) {
                // ignore exception
            }
        }
    }
}
