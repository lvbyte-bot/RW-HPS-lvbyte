# RW-HPS - Net

**Table of Contents**

- [1. Net](#1-net)
    - [Creating Net](#creating-net)
        - [Starting Port Listening](#starting-port-listening)
        - [Modifying Protocol](#modifying-protocol)

## 1. Net

During server startup, the server employs [CoreCommand: startnetservice] to initiate port listening.

### Creating Net

In the server, each port or port range is described by StartNet. The core of the server lies here.

```kotlin
class StartNet {
    // Default protocol
    constructor()

    // Custom protocol
    constructor(abstractNetClass: Class<*>)
}
```

Typically called as:

```
// Kotlin
val startNet = StartNet()

// Java
StartNet startNet = new StartNet();
```

#### Starting Port Listening

```
val startNet = ~
startNet.openPort(mainPort)
For listening on multiple ports, you can
startNet.openPort(mainPort, listeningRangeStart, listeningRangeEnd)
```

#### Modifying Protocol

Simply inherit AbstractNet to modify initChannel implementation and customize the protocol. In RW-HPS, a default GamePort shared parser is already implemented, which you can invoke as needed.

```kotlin
class NewShunt(startNet: StartNet) : AbstractNet(startNet) {
    override fun initChannel(socketChannel: SocketChannel) {
        // Unleash your creativity
        socketChannel.pipeline().addLast()
    }
}
```