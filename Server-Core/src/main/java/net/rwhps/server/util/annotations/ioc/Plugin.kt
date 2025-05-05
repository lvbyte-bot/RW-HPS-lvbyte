/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util.annotations.ioc

import net.rwhps.server.util.annotations.DidNotFinish

/**
 * 自动扫描注解-扫描Jar的插件类
 * @property pluginEnum 类型
 *
 * @date 2024/5/10 下午8:30
 * @author Dr (dr@der.kim)
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS)
@DidNotFinish
annotation class Plugin(
    val pluginEnum: PluginEnum
) {
    companion object {
        enum class PluginEnum {
            Main,
            Event,
            Command,
        }
    }
}
