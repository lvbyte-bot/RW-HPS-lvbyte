/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util.file.plugin.value

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 可被 Kotlin 委托使用的 [Value]
 *
 * @date 2024/2/15 12:33
 * @author Dr (dr@der.kim)
 */
class DelegatedValue<T>(
    value: T
) : SerializableValue<T>(value), ReadWriteProperty<Any, T> {

    @JvmSynthetic
    override fun getValue(thisRef: Any, property: KProperty<*>): T = value

    @JvmSynthetic
    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        this.value = value
    }
}