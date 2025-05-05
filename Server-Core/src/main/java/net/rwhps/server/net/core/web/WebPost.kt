/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net.core.web

import net.rwhps.server.util.file.json.Json
import net.rwhps.server.net.http.AcceptWeb
import net.rwhps.server.net.http.SendWeb
import net.rwhps.server.util.inline.ifNullResult
import net.rwhps.server.util.log.exp.ImplementedException

/**
 * @author Dr (dr@der.kim)
 */
abstract class WebPost: WebFunction() {
    /**
     * 开发者不应该在类中缓存任何数据
     * 因为 GET/POST 不是长链接 只是一个短链接 当请求发送完毕 那么就会被 Close
     * 支持 Keep, 但不能识别连接
     * HTTP Service 仅对 本次的调用负责
     * 是线程不安全的
     */
    /**
     * 处理数据
     * @param accept AcceptWeb
     * @param send SendWeb
     */
    abstract fun post(accept: AcceptWeb, send: SendWeb)

    protected fun stringPostDataResolveToJson(accept: AcceptWeb): Json {
        if (accept.data.isEmpty()) {
            return Json(LinkedHashMap<String, String>())
        }
        val hd = accept.getHeaders("Content-Type").ifNullResult("") { it.trim() }
        return if (hd.contains("application/x-www-form-urlencoded", true)) {
            val paramArray: Array<String> = accept.data.split("&".toRegex()).toTypedArray()
            val listMap = LinkedHashMap<String, String>()
            for (pam in paramArray) {
                val keyValue = pam.split("=".toRegex()).toTypedArray()
                listMap[keyValue[0]] = keyValue[1]
            }
            Json(listMap)
        } else if (hd.contains("application/json", true)) {
            Json(accept.data)
        } else {
            throw ImplementedException(accept.data)
        }
    }
}