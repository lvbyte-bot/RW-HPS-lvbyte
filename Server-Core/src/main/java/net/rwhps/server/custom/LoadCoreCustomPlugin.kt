/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.custom

import net.rwhps.server.data.bean.internal.BeanPluginInfo
import net.rwhps.server.data.global.Data
import net.rwhps.server.plugin.beta.ConnectLimit
import net.rwhps.server.plugin.beta.UpListMain
import net.rwhps.server.plugin.beta.bind.PlayerBindMain
import net.rwhps.server.plugin.beta.game.ClosingBorder
import net.rwhps.server.plugin.beta.game.NeverEndGame
import net.rwhps.server.plugin.beta.http.RwHpsWebApiMain
import net.rwhps.server.plugin.internal.headless.HessMain
import net.rwhps.server.plugin.internal.moreheadless.MoreHessMain
import net.rwhps.server.util.file.plugin.PluginManage

/**
 * 内部的一些插件 加载
 *
 * @author Dr (dr@der.kim)
 */
internal class LoadCoreCustomPlugin {
    private val core = "[Core Plugin]"
    private val coreEx = "[Core Plugin Extend]"
    private val amusement = "[Amusement Plugin]"
    private val example = "[Example Plugin]"

    init {
        PluginManage.addPluginClass(returnTemplate(
                "Headless Rusted Warfare" ,"$core Headless Rusted Warfare", "HessServer"
        ), HessMain(), mkdir = false, skip = true)

        PluginManage.addPluginClass(returnTemplate(
                "UpList", "$coreEx UpList", "1.0.0"
        ), UpListMain(), mkdir = false, skip = true)
        PluginManage.addPluginClass(returnTemplate(
                "ConnectLimit", "$coreEx ConnectLimit"
        ), ConnectLimit(), mkdir = false, skip = true)
        PluginManage.addPluginClass(returnTemplate(
                "RW-HPS Web Api", "$coreEx API interface for RW-HPS", "HttpApi"
        ), RwHpsWebApiMain(), mkdir = true, skip = true)
        PluginManage.addPluginClass(returnTemplate(
                "BindPlayer" ,"$coreEx player bind"
        ), PlayerBindMain(), mkdir = true, skip = true)

        //PluginManage.addPluginClass("DataCollectionBackend","Dr","$coreEx DataCollectionBackend","1.2", StatisticsBackEnd(),false)
    }

    private fun returnTemplate(name: String, description: String, internalName: String = name, version: String = Data.SERVER_CORE_VERSION): BeanPluginInfo {
        return BeanPluginInfo(
                name, internalName,
                author = "Dr (dr@der.kim)",
                description = description, version = version,
                supportedVersions = "= ${Data.SERVER_CORE_VERSION}"
        )
    }
}