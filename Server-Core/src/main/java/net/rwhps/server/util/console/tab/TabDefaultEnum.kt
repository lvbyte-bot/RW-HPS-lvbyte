/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util.console.tab

import net.rwhps.server.util.EnumUtils

/**
 *
 *
 * @date 2024/2/7 9:09
 * @author Dr (dr@der.kim)
 */
enum class TabDefaultEnum(
    val tabCompletion: AbstractTabCompletion
) {
    PlayerPosition(PlayerPositionCompletion(onlyAi = false, onlyPlayer = false)),
    PlayerPositionAI(PlayerPositionCompletion(onlyAi = true, onlyPlayer = false)),
    PlayerPositionNoAI(PlayerPositionCompletion(onlyAi = false, onlyPlayer = true)),
    BanList(BanListCompletion()),

    Language(LanguageCompletion());

    companion object {
        fun from(name: String): TabDefaultEnum? = EnumUtils.from(entries, name)
    }
}
