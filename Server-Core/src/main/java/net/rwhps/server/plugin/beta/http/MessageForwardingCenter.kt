/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.beta.http

import io.netty.handler.codec.http.HttpHeaderValues
import net.rwhps.server.game.manage.ModManage
import net.rwhps.server.net.core.web.WebGet
import net.rwhps.server.net.core.web.WebPost
import net.rwhps.server.net.http.AcceptWeb
import net.rwhps.server.net.http.SendWeb
import net.rwhps.server.plugin.beta.http.WebStatus.Companion.toWebStatusJson
import net.rwhps.server.plugin.beta.http.data.GetData
import net.rwhps.server.plugin.beta.http.post.run.ClientCommand
import net.rwhps.server.plugin.beta.http.post.run.ServerCommand
import net.rwhps.server.util.inline.toJson

/**
 * @date  2023/6/27 10:49
 * @author Dr (dr@der.kim)
 */
class MessageForwardingCenter {
    val getCategorize = object: WebGet() {
        override fun get(accept: AcceptWeb, send: SendWeb) {
            send.setConnectType(HttpHeaderValues.APPLICATION_JSON)
            when (accept.getUrl.removePrefix("/${RwHpsWebApiMain.name}/api/get/")) {
                "event/GameOver" -> {
                    send.setData(GetData.GameOverPositive.data.data.toList().toJson().toWebStatusJson())
                    GetData.GameOverPositive.data.data.clear()
                }
                "info/SystemInfo" -> send.setData(GetData.SystemInfo().toJson().toWebStatusJson())
                "info/GameInfo" -> send.setData(GetData.GameInfo().toJson().toWebStatusJson())
                "info/ModsInfo" -> send.setData(ModManage.getModsList().toJson().toWebStatusJson())
                else -> send.send404(false)
            }
            send.send()
        }
    }

    val postCategorize = object: WebPost() {
        override fun post(accept: AcceptWeb, send: SendWeb) {
            send.setConnectType(HttpHeaderValues.APPLICATION_JSON)
            when (accept.getUrl.removePrefix("/${RwHpsWebApiMain.name}/api/post/")) {
                "run/${ServerCommand.prefixURL}" -> ServerCommand.post(accept, send)
                "run/${ClientCommand.prefixURL}" -> ClientCommand.post(accept, send)
                else -> send.send404(false)
            }
            send.send()
        }
    }
}