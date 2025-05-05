/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */
package net.rwhps.server.func

import java.text.MessageFormat

/**
 * Log使用
 *
 * @author Dr (dr@der.kim)
 */
fun interface StrCons {
    /**
     * Log 专用
     *
     * @param str String
     */
    operator fun invoke(str: String)

    /**
     * Log 转换
     *
     * @param t   String
     * @param obj Object...
     */
    operator fun invoke(t: String, vararg obj: Any?) {
        invoke(MessageFormat(t).format(obj))
    }
}
