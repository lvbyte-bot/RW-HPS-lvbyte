/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.internal.headless.inject.core.link

import net.rwhps.server.game.headless.core.link.AbstractLinkGameFunction
import net.rwhps.server.net.core.IRwHps
import net.rwhps.server.plugin.internal.headless.inject.core.GameEngine
import net.rwhps.server.util.file.FileName.getFileNameNoSuffix
import net.rwhps.server.util.log.Log

/**
 * @author Dr (dr@der.kim)
 */
internal class LinkGameFunction: AbstractLinkGameFunction {
    override fun allPlayerSync() {
        try {
            synchronized(GameEngine.gameEngine) {
                GameEngine.netEngine.a(false, false, true)
            }
        } catch (e: Exception) {
            Log.error(e.cause!!)
        }
    }

    override fun pauseGame(pause: Boolean) {
        GameEngine.netEngine.e(pause)
    }

    override fun battleRoom(time: Int) {
        GameEngine.netEngine.d(time.toFloat())
        GameEngine.data.room.battleRoom()
    }

    override fun saveGame() {
        GameEngine.gameEngine.ca.b(getFileNameNoSuffix(GameEngine.data.room.replayFileName), false)
        Log.clog("Save: ${getFileNameNoSuffix(GameEngine.data.room.replayFileName)}.rwsave")
    }

    override fun clean() {
        if (GameEngine.iRwHps == null || GameEngine.iRwHps!!.netType != IRwHps.NetType.ServerProtocol) {
            return
        }
        GameEngine.gameEngine.bX.b("exited")
        GameEngine.root.showMainMenu()
        InterruptedException().printStackTrace()
    }
}