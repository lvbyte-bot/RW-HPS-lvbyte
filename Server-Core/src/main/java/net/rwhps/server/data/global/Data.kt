/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.data.global

import net.rwhps.server.command.ex.Vote
import net.rwhps.server.core.Application
import net.rwhps.server.data.bean.BeanCoreConfig
import net.rwhps.server.data.bean.BeanRelayConfig
import net.rwhps.server.data.bean.BeanServerConfig
import net.rwhps.server.data.bean.internal.BeanHeadlessConfig
import net.rwhps.server.data.bean.internal.BeanMainParameters
import net.rwhps.server.data.bean.internal.BeanNetConfig
import net.rwhps.server.func.StrCons
import net.rwhps.server.net.http.WebData
import net.rwhps.server.struct.map.ObjectMap
import net.rwhps.server.util.SystemUtils
import net.rwhps.server.util.annotations.mark.PrivateMark
import net.rwhps.server.util.file.load.I18NBundle
import net.rwhps.server.util.file.load.LoadIni
import net.rwhps.server.util.game.command.CommandHandler
import net.rwhps.server.util.log.Log
import org.jline.reader.LineReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * @author Dr (dr@der.kim)
 */
object Data {
    /** 服务器主目录 */
    const val ServerDataPath = "/data"

    /** 服务器save保存目录 */
    const val ServerSavePath = "/data/saves"

    /** 服务器缓存目录 */
    const val ServerCachePath = "/data/cache"

    /** 服务器依赖目录 */
    const val ServerLibPath = "/data/libs"

    /** 服务器日志目录 */
    const val ServerLogPath = "/data/log"

    const val ServerPlayerPath = "/data/player"

    /** 服务器地图目录 */
    const val ServerMapsPath = "/data/maps"

    /** 服务器插件目录 */
    const val ServerPluginsPath = "/data/plugins"

    /** 服务器无头数据目录 */
    const val Plugin_GameCore_Data_Path = "/data/gameData"

    /** 服务器Mod目录 */
    const val Plugin_Mods_Path = "/data/mods"

    /** 服务器保存RePlay目录 */
    const val Plugin_RePlays_Path = "/data/replays"

    /** 服务器 UTF-8 缓存 */
    @JvmField
    val UTF_8: Charset = StandardCharsets.UTF_8

    /** 服务器默认编码缓存 */
    @JvmField
    val DefaultEncoding: Charset = SystemUtils.defaultEncoding

    /*
	 * 插件默认变量
	 */

    /** 自定义包名  */
    const val SERVER_ID = "net.rwhps.server"

    /** 自定义 RELAY ID */
    const val SERVER_ID_RELAY = "net.rwhps.server.relayCustomMode.Dr"

    /** RELAY诱骗 UUID-Hex 时的 UUID */
    const val SERVER_ID_RELAY_GET = "net.rwhps.server.relayGetUUIDHex.Dr"

    /** 服务器主版本 */
    const val SERVER_CORE_VERSION = "3.0.0"

    /** 服务器Topt密码  */
    const val TOPT_KEY = "net.rwhps.server.topt # Dr (dr@der.kim)"

    /** RELAY 使用的 UUID */
    const val SERVER_RELAY_UUID = "Dr (dr@der.kim) & Tiexiu.xyz Core Team"

    /** EULA 的版本 */
    const val SERVER_EULA_VERSION = "1.1.2"

    const val userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36 Edg/114.0.1823.51"


    @JvmField
    val LINE_SEPARATOR: String = System.getProperty("line.separator")

    /** 服务端 服务端命令  */
    @JvmField
    val SERVER_COMMAND = CommandHandler("")

    /** 服务端 Log命令  */
    @JvmField
    val LOG_COMMAND = CommandHandler("!")

    /** 服务端 Relay命令  */
    @JvmField
    @PrivateMark
    val RELAY_COMMAND = CommandHandler(".")

    internal val PING_COMMAND = CommandHandler("")

    @JvmField
    val core = Application()

    @JvmField
    val i18NBundleMap = ObjectMap<String, I18NBundle>(8)

    @JvmField
    val urlData: LoadIni = LoadIni(Data::class.java.getResourceAsStream("/URL.ini")!!)

    /** 服务端 核心配置  */
    lateinit var config: BeanCoreConfig
    val configNet by lazy { BeanNetConfig() }
    val configHeadless by lazy { BeanHeadlessConfig() }

    /** 服务端 Server配置  */
    lateinit var configServer: BeanServerConfig

    /** 服务端 Relay配置  */
    @PrivateMark
    lateinit var configRelay: BeanRelayConfig

    /** 服务器默认 WebData 数据 */
    val webData = WebData()

    internal lateinit var mainParameters: BeanMainParameters

    /**
     * 可控变量
     */
    lateinit var i18NBundle: I18NBundle

    @JvmField
    var vote: Vote? = null

    // TODO
    var bindForcibly = true

    @Volatile
    var startServer = false

    val headlessName: String = "RW-HPS Core Headless"

    internal val privateOut = System.out
    internal lateinit var privateReader: LineReader

    @JvmField
    val defPrint = StrCons { obj: String -> Log.clog(obj) }

    var serverCountry = "EN"

    val neverEnd=false
}