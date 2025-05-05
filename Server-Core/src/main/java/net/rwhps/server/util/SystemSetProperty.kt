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
 * @date  2023/5/25 12:00
 * @author Dr (dr@der.kim)
 */
object SystemSetProperty {
    /**
     * 解决 Jline 在 Idea 上的问题
     */
    fun setJlineIdea() {
        System.setProperty("org.jline.terminal.dumb", "true")
        /* Fix Idea */
        System.setProperty("jansi.passthrough", "true")
    }

    /**
     * 关闭IPV6监听
     */
    fun setOnlyIpv4() {
        // F U C K IPV6
        System.setProperty("java.net.preferIPv6Stack", "false")
        System.setProperty("java.net.preferIPv4Stack", "true")
    }

    /**
     * 无头
     */
    fun setAwtHeadless() {
        // F U C K Termux
        System.setProperty("java.awt.headless", "true")
    }
}