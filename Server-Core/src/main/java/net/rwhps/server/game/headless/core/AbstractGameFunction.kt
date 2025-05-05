/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.headless.core

/**
 * 游戏函数扩展
 *
 * @date 2023/12/22 17:12
 * @author Dr (dr@der.kim)
 */
interface AbstractGameFunction {
    /**
     * 暂停游戏 Loop 线程, 来完成一些避免问题的操作
     *
     * @param run 需要运行的 [Runnable]
     */
    fun suspendMainThreadOperations(run: Runnable)

    val neverEnd: IntArray
}