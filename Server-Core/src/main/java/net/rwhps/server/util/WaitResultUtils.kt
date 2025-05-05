/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util

/**
 * 等待并且获取 直到得到有效值
 * @author Dr (dr@der.kim)
 */
object WaitResultUtils {
    @Throws(NullPointerException::class)
    fun <T> waitResult(sleep: Long = 10, block: () -> T?): T? {
        var result: T? = block()
        var count = 0
        while (result == null) {
            if (count > 10) {
                return null
            }
            Thread.sleep(sleep)
            result = block()
            count++
        }
        return result
    }
}