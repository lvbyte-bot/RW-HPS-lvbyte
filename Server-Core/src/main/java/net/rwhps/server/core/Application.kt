/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.core

import net.rwhps.server.core.thread.Threads
import net.rwhps.server.core.thread.Threads.close
import net.rwhps.server.core.thread.Threads.runSavePool
import net.rwhps.server.data.global.Data
import net.rwhps.server.net.Administration
import net.rwhps.server.net.manage.HttpRequestManage
import net.rwhps.server.net.manage.MyOkHttpRetryInterceptor
import net.rwhps.server.util.algorithms.digest.DigestUtils
import net.rwhps.server.util.file.FileUtils
import net.rwhps.server.util.file.plugin.PluginData
import net.rwhps.server.util.file.plugin.PluginManage.runOnDisable
import net.rwhps.server.util.file.plugin.serializer.SerializersEnum
import net.rwhps.server.util.file.plugin.value
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.math.RandomUtils.getRandomString
import okhttp3.OkHttpClient
import java.math.BigInteger
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


/**
 * @author Dr (dr@der.kim)
 */
class Application {
    /** 服务器 Setting 主数据 */
    val settings: PluginData = PluginData(SerializersEnum.Bin.create()).apply {
        setFileUtil(FileUtils.getFolder(Data.ServerDataPath).toFile("Settings.bin"))
    }

    /** 服务器唯一UUID  */
    val serverConnectUuid by settings.value(UUID.randomUUID().toString())
    /** Hess HEX */
    val serverHessUuid by settings.value(BigInteger(1, DigestUtils.sha256(serverConnectUuid + UUID.randomUUID().toString())).toString(16).uppercase())

    @JvmField
    var serverToken: String = getRandomString(40)
    val admin: Administration = Administration(settings)

    @JvmField
    var upServerList = false

    val http = HttpRequestManage()
    val rwHttp = HttpRequestManage(OkHttpClient.Builder().also { builder ->
        builder.retryOnConnectionFailure(true)
        builder.addInterceptor(MyOkHttpRetryInterceptor.Builder().executionCount(5).retryInterval(2500).build())
        builder.connectTimeout(10, TimeUnit.SECONDS)
        builder.readTimeout(10, TimeUnit.SECONDS)
        builder.writeTimeout(10, TimeUnit.SECONDS)
    }.build(), "RW-HPS ${Data.SERVER_CORE_VERSION}")

    /**
     * 服务器退出时保存数据
     */
    fun save() {
        // 先执行自己的保存
        runSavePool()
        // Save Data
        Threads.dataAutoSave.eachAll(Runnable::run)
        // 保存Plugin
        runOnDisable()

        Log.saveLog()
    }

    /**
     * Exit and save data
     */
    @JvmOverloads
    inline fun exit(run: () -> Unit = {}) {
        try {
            NetServer.closeServer()
            save()
            close()
            Thread.setDefaultUncaughtExceptionHandler(null)
            exitProcess(0)
        } finally {
            run()
        }
    }

    /**
     * Force quit
     */
    fun mandatoryExit() {
        exitProcess(0)
    }
}