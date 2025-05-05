/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.internal.headless.inject.core.link

import com.corrodinggames.rts.game.n
import net.rwhps.server.game.headless.core.link.AbstractLinkGameServerData
import net.rwhps.server.game.headless.core.link.AbstractLinkPlayerData
import net.rwhps.server.plugin.internal.headless.inject.core.GameEngine
import net.rwhps.server.util.WaitResultUtils
import net.rwhps.server.util.inline.findField
import net.rwhps.server.util.log.exp.ImplementedException

/**
 * Link The game comes with settings to avoid some of the distractions caused by confusion
 *
 * @author Dr (dr@der.kim)
 */
class LinkGameServerData: AbstractLinkGameServerData {
    override val teamOperationsSyncObject: Any get() = GameEngine.netEngine::class.java.findField("bC")!!.get(GameEngine.netEngine)

    override var maxUnit: Int
        set(value) {
            GameEngine.netEngine.ax = value
            GameEngine.netEngine.aw = value
        }
        get() = GameEngine.netEngine.ax

    override var sharedcontrol: Boolean
        set(value) {
            GameEngine.netEngine.ay.l = value
        }
        get() = GameEngine.netEngine.ay.l

    override var fog: Int
        set(value) {
            GameEngine.netEngine.ay.d = value
        }
        get() = GameEngine.netEngine.ay.d

    override var nukes: Boolean
        set(value) {
            GameEngine.netEngine.ay.i = value
        }
        get() = GameEngine.netEngine.ay.i

    override var credits: Int
        set(value) {
            GameEngine.netEngine.ay.c = value
        }
        get() = GameEngine.netEngine.ay.c

    override var aiDifficuld: Int
        set(value) {
            GameEngine.netEngine.ay.f = value
        }
        get() = GameEngine.netEngine.ay.f

    override var income: Float
        set(value) {
            GameEngine.netEngine.ay.h = value
        }
        get() = GameEngine.netEngine.ay.h

    override var startingunits: Int
        set(value) {
            GameEngine.netEngine.ay.g = value
        }
        get() = GameEngine.netEngine.ay.g

    override fun getPlayerData(position: Int): AbstractLinkPlayerData {
        return PrivateClassLinkPlayer(WaitResultUtils.waitResult { n.k(position) } ?: throw ImplementedException.PlayerImplementedException(
                "[PlayerData-New] Player is invalid"
        ))
    }

    override fun getPlayerAIData(position: Int): AbstractLinkPlayerData {
        return PrivateClassLinkAIPlayer(WaitResultUtils.waitResult { n.k(position) } ?: throw ImplementedException.PlayerImplementedException(
                "[PlayerData-New] AI is invalid"
        ))
    }
}