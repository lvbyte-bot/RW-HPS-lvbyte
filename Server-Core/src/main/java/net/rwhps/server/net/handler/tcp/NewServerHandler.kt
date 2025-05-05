/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net.handler.tcp

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import net.rwhps.server.game.event.global.NetConnectNewEvent
import net.rwhps.server.io.packet.Packet
import net.rwhps.server.net.core.AbstractNet
import net.rwhps.server.net.core.ConnectionAgreement
import net.rwhps.server.net.core.INetServerHandler
import net.rwhps.server.util.file.plugin.PluginManage
import net.rwhps.server.util.log.Log.debug
import net.rwhps.server.util.log.Log.error
import net.rwhps.server.util.log.exp.ExceptionX

/**
 *
 * @author Dr (dr@der.kim)
 */
@Sharable
internal class NewServerHandler(abstractNet: AbstractNet): INetServerHandler(abstractNet) {

    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any?) {
        if (msg == null) {
            return
        }

        try {
            if (msg is Packet) {
                val attr = abstractNet.getTypeConnect(ctx.channel())
                var type = attr.get()

                if (type == null) {
                    val connectionAgreement = ConnectionAgreement(ctx, abstractNet.nettyChannelData, abstractNet.rwHps)
                    type = abstractNet.rwHps.typeConnect.getTypeConnect(connectionAgreement)
                    attr.set(type)

                    val newConnectEvent = NetConnectNewEvent(connectionAgreement)
                    PluginManage.runGlobalEventManage(newConnectEvent).await()
                    if (newConnectEvent.result) {
                        type.abstractNetConnect.disconnect()
                        return
                    }
                }
                try {
                    type.processConnect(msg)
                } catch (e: Exception) {
                    debug(e)
                }
            }
        } catch (ss: Exception) {
            error(ss)
        }
    }


    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable?) {
        cause?.let {
            error(ExceptionX.resolveTrace(it))
        }
    }
}