/*
 *
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 *
 */

package net.rwhps.server.plugin.beta.http

import io.netty.channel.Channel
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import net.rwhps.server.data.global.Data
import net.rwhps.server.util.file.json.Json
import net.rwhps.server.plugin.beta.http.data.GetData
import net.rwhps.server.util.inline.toJson

/**
 * @date 2023/7/16 14:51
 * @author Dr (dr@der.kim)
 */
class ConsoleWebSocket {
    fun ws(channel: Channel, json: Json) {
        when (json.getString("type", "")) {
            "register" -> channel.writeAndFlush(TextWebSocketFrame("Register OK".toWebStatusJson()))
            "ping" -> channel.writeAndFlush(TextWebSocketFrame("pong".toWebStatusJson()))
            "getConsole" -> {
                if (GetData.agentConsole.contains(channel.id().toString())) {
                    channel.writeAndFlush(TextWebSocketFrame("Repeat".toWebStatusJson(403)))
                    return
                }
                GetData.agentConsole.add(channel.id().toString())
                channel.writeAndFlush(TextWebSocketFrame(GetData.consoleCache.data.joinToString(Data.LINE_SEPARATOR).toWebStatusJson()))
                GetData.agentConsoleLog[channel.id().toString()] = {
                    channel.writeAndFlush(TextWebSocketFrame(it.toWebStatusJson(1000)))
                }
            }
            "runCommand" -> {
                Data.SERVER_COMMAND.handleMessage(json.getString("runCommand"), Data.defPrint)
            }
        }
    }

    fun closeWs(channel: Channel) {
        GetData.agentConsole.remove(channel.id().toString())
        GetData.agentConsoleLog.remove(channel.id().toString())
    }


    /**
     * @date  2023/7/1 16:30
     * @author Dr (dr@der.kim)
     */
    private data class WebStatus(
        val code: Int, val data: String
    )

    private fun String.toWebStatus(code: Int = 200): WebStatus {
        return WebStatus(code, this)
    }

    private fun String.toWebStatusJson(code: Int = 200): String {
        return WebStatus(code, this).toJson()
    }
}