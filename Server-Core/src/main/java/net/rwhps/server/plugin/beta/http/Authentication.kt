/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.beta.http

import io.netty.channel.Channel
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.util.AttributeKey
import net.rwhps.server.data.global.Data
import net.rwhps.server.util.file.json.Json
import net.rwhps.server.net.core.web.WebGet
import net.rwhps.server.net.core.web.WebPost
import net.rwhps.server.net.core.web.WebSocket
import net.rwhps.server.net.handler.tcp.StartWebSocket
import net.rwhps.server.net.http.AcceptWeb
import net.rwhps.server.net.http.SendWeb
import net.rwhps.server.struct.map.ObjectIntMap
import net.rwhps.server.struct.map.ObjectMap
import net.rwhps.server.util.math.RandomUtils
import net.rwhps.server.util.Time
import net.rwhps.server.util.algorithms.digest.DigestUtils

/**
 * @date  2023/6/27 11:07
 * @author Dr (dr@der.kim)
 */
class Authentication(private val messageForwardingCenter: MessageForwardingCenter) {
    private val cookiesData = ObjectIntMap<String>()

    fun registerAuthenticationCenter() {
        Data.webData.addWebGetInstance("/${RwHpsWebApiMain.name}/api/AuthCookie", object: WebGet() {
            override fun get(accept: AcceptWeb, send: SendWeb) = registerCookie(stringUrlDataResolveToJson(accept), accept, send)
        })
        Data.webData.addWebGetInstance("/${RwHpsWebApiMain.name}/api/get/**", object: WebGet() {
            override fun get(accept: AcceptWeb, send: SendWeb) {
                val cookies = headResolveToCookie(accept)
                if (authentication(cookies, send)) {
                    messageForwardingCenter.getCategorize.get(accept, send)
                }
            }
        })

        Data.webData.addWebPostInstance("/${RwHpsWebApiMain.name}/api/AuthCookie", object: WebPost() {
            override fun post(accept: AcceptWeb, send: SendWeb) = registerCookie(stringPostDataResolveToJson(accept), accept, send)
        })
        Data.webData.addWebPostInstance("/${RwHpsWebApiMain.name}/api/post/**", object: WebPost() {
            override fun post(accept: AcceptWeb, send: SendWeb) {
                val cookies = headResolveToCookie(accept)
                if (authentication(cookies, send)) {
                    messageForwardingCenter.postCategorize.post(accept, send)
                }
            }
        })

        Data.webData.addWebSocketInstance("/WebSocket/${RwHpsWebApiMain.name}/api/Console", object: WebSocket() {
            val NETTY_CHANNEL_KEY = AttributeKey.valueOf<ConsoleWebSocket>("${RwHpsWebApiMain.name}-Ws")!!

            override fun ws(ws: StartWebSocket, channel: Channel, msg: String) {
                try {
                    val json = Json(msg)

                    val attr = channel.attr(NETTY_CHANNEL_KEY)
                    var type = attr.get()

                    if (type == null) {
                        if (cookiesData[json.getString("cookie", ""), 0] < Time.concurrentSecond()) {
                            channel.writeAndFlush(
                                    msg(
                                            """
                                {
                                    "code" : 401, "data" : "[Unauthorized] The cookies expires"
                                }
                            """.trimIndent()
                                    )
                            )
                            return
                        }
                        type = ConsoleWebSocket()
                        attr.setIfAbsent(type)
                    }

                    type.ws(channel, json)
                } catch (e: Exception) {
                    channel.writeAndFlush(
                            msg(
                                    """
                        {
                            "code" : 400,
                            "data" : "[Bad Request] 请传入正确 Json !"
                        }
                    """.trimIndent()
                            )
                    )
                }
            }

            override fun closeWs(ws: StartWebSocket, channel: Channel) {
                val attr = channel.attr(NETTY_CHANNEL_KEY)
                val type = attr.get()
                type?.closeWs(channel)
            }
        })
    }

    private fun registerCookie(json: Json, accept: AcceptWeb, send: SendWeb) {
        if (accept.getHeaders(HttpHeaderNames.ORIGIN) != null) {
            // 允许跨域
            send.setHead(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, accept.getHeaders(HttpHeaderNames.ORIGIN)!!)
            send.setHead(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
        }

        if (json.getString("passwd") == Data.config.webToken) {
            val cValue = DigestUtils.sha256Hex(RandomUtils.getRandomString(32))
            send.setCookie(RwHpsWebApiMain.cookieName, cValue, 86400, RwHpsWebApiMain.name)
            cookiesData[cValue] = Time.concurrentSecond() + 86400
            send.setData("Hi RW-HPS !")
            send.send()
        } else {
            send.status = HttpResponseStatus.FORBIDDEN
            send.setData("[Forbidden] Input error")
            send.send()
        }
    }

    private fun authentication(cookies: ObjectMap<String, String>, send: SendWeb): Boolean {
        if (cookies.isEmpty()) {
            send.status = HttpResponseStatus.UNAUTHORIZED
            send.setData("[Unauthorized] Please certify")
            send.send()
            return false
        }

        if (cookiesData[cookies[RwHpsWebApiMain.cookieName, ""], 0] < Time.concurrentSecond()) {
            send.status = HttpResponseStatus.UNAUTHORIZED
            send.setData("[Unauthorized] The cookies expires")
            send.send()
            return false
        }
        return true
    }
}
//{
//    "cookie" : "5420dedf8f1829bc43d03843d26216523fe19e06ee253abcacd3a4ee5b9af12b",
//    "type" : "register"
//}

//{
//    "type" : "ping"
//}

//{
//    "type" : "getConsole"
//}

//{
//    "type" : "runCommand",
//    "runCommand" : "version"
//}