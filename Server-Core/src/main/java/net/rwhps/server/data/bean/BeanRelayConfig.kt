/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.data.bean

import net.rwhps.server.data.global.Data
import net.rwhps.server.util.annotations.mark.PrivateMark
import net.rwhps.server.util.file.FileUtils
import net.rwhps.server.util.inline.toGson


/**
 * Relay-Protocol configuration file
 *
 * Save data for serialization and deserialization
 * @author Dr (dr@der.kim)
 */
@PrivateMark
data class BeanRelayConfig(
    val mainID: String = "R",
    val mainServer: Boolean = true,
    val upList: Boolean = true,
    val mainServerIP: String = "relay.der.kim",
    val mainServerPort: Int = 4993
): AbstractBeanConfig(
        this::class.java, "rwhps.config.relay"
) {

    companion object {
        val fileUtils = FileUtils.getFolder(Data.ServerDataPath).toFile("RelayConfig.json")

        @JvmStatic
        fun stringToClass(): BeanRelayConfig {

            val config: BeanRelayConfig = BeanRelayConfig::class.java.toGson(fileUtils.readFileStringData())
            config.bindFile(fileUtils)
            config.readProperty()
            return config
        }
    }
}