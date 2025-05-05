/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util.internal.kotlin

/**
 * Kotlin的一些离谱用法, 个人使用/看
 *
 * @date 2024/2/15 11:34
 * @author Dr (dr@der.kim)
 */
@Suppress("UNUSED")
class SuppressProMax {
    fun a() {
        val some: List<*> = emptyList<Nothing>()
        @Suppress("UNCHECKED_CAST")
        some as List<String>
    }

    fun npeError() {
        @Suppress("UNSAFE_CALL")
        println((null as String?).length)
    }

    fun arrayPro() {
        fun some(any: Any) {
            @Suppress("CANNOT_CHECK_FOR_ERASED")
            when(any) {
                // Smart cast to Array<String>
                is Array<String> -> println(any.size)
            }
        }
    }

    @Suppress("INAPPLICABLE_LATEINIT_MODIFIER")
    private lateinit val lateInitVal: String
    @Suppress("VAL_REASSIGNMENT")
    fun lateinitVal() {
        lateInitVal = "1"
        println(lateInitVal) // 输出 1
        lateInitVal = "2"
        println(lateInitVal) // 输出 2
    }
}