/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.command.relay

import net.rwhps.server.data.global.Data
import net.rwhps.server.func.StrCons
import net.rwhps.server.game.room.RelayRoom
import net.rwhps.server.util.IpUtils
import net.rwhps.server.util.IsUtils.isBlank
import net.rwhps.server.util.annotations.mark.PrivateMark
import net.rwhps.server.util.file.plugin.PluginManage
import net.rwhps.server.util.game.command.CommandHandler

/**
 * @author Dr (dr@der.kim)
 */
@PrivateMark
class RelayCommands(handler: CommandHandler) {
    private fun registerRelayCommand(handler: CommandHandler) {
    }

    companion object {
        private val localeUtil = Data.i18NBundle
    }

    init {
        registerRelayCommand(handler)

        PluginManage.runRegisterRelayCommands(handler)

        RelayClientCommands(Data.RELAY_COMMAND)
    }
}