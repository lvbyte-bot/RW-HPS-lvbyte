/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.core.thread

import net.rwhps.server.core.thread.CallGroupData.coreExpandServer
import net.rwhps.server.core.thread.CallGroupData.coreServer

/**
 * @author Dr (dr@der.kim)
 */
enum class CallTimeTask(
    val group: String, val description: String
) {
    CallTeamTask(coreServer, "Update player team list"),

    PlayerAfkTask(coreServer, "Transfer of authority"),
    GameOverTask(coreServer, "Check gameover"),
    VoteTask(coreServer, "Vote"),
    AutoStartTask(coreExpandServer, "Start automatically"),
    AutoUpdateMapsTask(coreExpandServer, "Auto update maps"),
    AutoCheckTask(coreExpandServer, "Auto Check player survives"),

    CustomUpServerListTask("UpList", "[Plugin UpList] Update Data"),
    UpServerListTask("UpList", "Core"),
    UpServerListNewTask("UpList", "Core"),


    BlackListCheckTask("[BlackList]", "超时去除"),

    ServerUploadDataTask("[RCN]", "列表更新"),
    ServerHostKeepAlive("[RCN]", "保活"),
    ServerUploadData_CheckTimeTask("[RCN]", "超时"),
    RelayRoom_CheckTimeTask("[RCN]", "房间超时机制"),
    ServerStatusUpdate("[RCN]", "状态更新"),


    ServerUpStatistics("[RW-HPS]", "统计数据更新")
}