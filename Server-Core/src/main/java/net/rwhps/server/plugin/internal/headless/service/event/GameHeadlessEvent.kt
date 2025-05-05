/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.internal.headless.service.event

import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.Statisticians
import net.rwhps.server.game.event.core.EventListenerHost
import net.rwhps.server.game.event.game.PlayerJoinEvent
import net.rwhps.server.game.event.game.PlayerOperationUnitEvent
import net.rwhps.server.game.event.global.ServerHessLoadEvent
import net.rwhps.server.game.manage.HeadlessModuleManage
import net.rwhps.server.game.manage.ModManage
import net.rwhps.server.util.annotations.core.EventListenerHandler
import net.rwhps.server.util.log.Log

class GameHeadlessEvent: EventListenerHost {
    @EventListenerHandler
    fun registerPlayerJoin(playerJoinEvent: PlayerJoinEvent) {
        playerJoinEvent.gameModule.room.apply {
            if (isStartGame) {
                if (sync) {
                    playerJoinEvent.player.kickPlayer("[Sync Lock] 这个房间拒绝重连")
                    return
                }
            }
        }
    }

    @EventListenerHandler
    fun registerPlayerJoin(playerOperationUnitEvent: PlayerOperationUnitEvent) {
//        playerOperationUnitEvent.gameModule.room.apply {
//            if (banUnit.find { playerOperationUnitEvent.unitName?.startsWith(it) == true } != null) {
//                playerOperationUnitEvent.resultStatus = false
//                return
//            }
//        }
    }
}