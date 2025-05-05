/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.event.game

import net.rwhps.server.game.event.core.AbstractEventImpl
import net.rwhps.server.game.headless.core.AbstractGameModule
import net.rwhps.server.game.player.PlayerHess

/**
 * 玩家离开服务器事件
 *
 * @date 2023/7/5 13:43
 * @author Dr (dr@der.kim)
 */
class PlayerLeaveEvent(
    override val gameModule: AbstractGameModule,
    val player: PlayerHess
): AbstractEventImpl