/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game

import net.rwhps.server.core.thread.CallTimeTask
import net.rwhps.server.core.thread.Threads
import net.rwhps.server.data.global.Data
import net.rwhps.server.game.event.core.EventListenerHost
import net.rwhps.server.game.event.game.*
import net.rwhps.server.net.Administration.PlayerInfo
import net.rwhps.server.struct.list.Seq
import net.rwhps.server.util.Time.millis
import net.rwhps.server.util.annotations.core.EventListenerHandler
import net.rwhps.server.util.compression.CompressionEncoderUtils
import net.rwhps.server.util.file.FileName.getFileNameNoSuffix
import net.rwhps.server.util.file.FileUtils
import net.rwhps.server.util.inline.coverConnect
import net.rwhps.server.util.inline.toPrettyPrintingJson
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.log.Log.error
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * @author Dr (dr@der.kim)
 */
@Suppress("UNUSED", "UNUSED_PARAMETER")
class Event: EventListenerHost {
    @EventListenerHandler
    fun registerServerHessStartPort(serverHessStartPort: ServerHessStartPort) {
        serverHessStartPort.gameModule.gameLinkServerData.maxUnit = Data.configServer.maxUnit
        serverHessStartPort.gameModule.gameLinkServerData.income = Data.configServer.defIncome
    }

    @EventListenerHandler
    fun registerPlayerJoinEvent(playerJoinEvent: PlayerJoinEvent) {
        val player = playerJoinEvent.player
        if (player.name.isBlank() || player.name.length > 30) {
            player.kickPlayer(player.getinput("kick.name.failed"))
            return
        }

        if (Data.core.admin.bannedUUIDs.contains(player.connectHexID)) {
            try {
                player.kickPlayer(player.i18NBundle.getinput("kick.ban"))
            } catch (ioException: IOException) {
                error("[Player] Send Kick Player Error", ioException)
            }
            return
        }

        if (Data.core.admin.playerDataCache.containsKey(player.connectHexID)) {
            val info = Data.core.admin.playerDataCache[player.connectHexID]!!
            if (info.timesKicked > millis()) {
                try {
                    player.kickPlayer(player.i18NBundle.getinput("kick.you.time"))
                } catch (ioException: IOException) {
                    error("[Player] Send Kick Player Error", ioException)
                }
                return
            } else {
                player.muteTime = info.timeMute
            }
        }

        playerJoinEvent.gameModule.room.call.sendSystemMessage(Data.i18NBundle.getinput("player.ent", player.name))
        Log.clog("&c" + Data.i18NBundle.getinput("player.ent", player.name))

        if (Data.configServer.autoStartMinPlayerSize != -1 && playerJoinEvent.gameModule.room.playerManage.playerGroup.size >= Data.configServer.autoStartMinPlayerSize && !Threads.containsTimeTask(
                    CallTimeTask.AutoStartTask
            )) {
            var flagCount = 10
            Threads.newTimedTask(CallTimeTask.AutoStartTask, 0, 1, TimeUnit.SECONDS) {
                if (playerJoinEvent.gameModule.room.isStartGame) {
                    Threads.closeTimeTask(CallTimeTask.AutoStartTask)
                    return@newTimedTask
                }

                flagCount--

                if (flagCount > 0) {
                    playerJoinEvent.gameModule.room.call.sendSystemMessage(Data.i18NBundle.getinput("auto.start", flagCount))
                    return@newTimedTask
                }

                Threads.closeTimeTask(CallTimeTask.AutoStartTask)
                Threads.closeTimeTask(CallTimeTask.PlayerAfkTask)

                playerJoinEvent.gameModule.room.clientHandler.handleMessage("/start", null)
            }
        }

        if (Data.configServer.enterAd.isNotBlank()) {
            player.sendSystemMessage(Data.configServer.enterAd)
        }

        if (Data.configServer.saveRePlayFile) {
            playerJoinEvent.player.infoObject.run {
                FileUtils.getFolder(Data.ServerPlayerPath).toFile("${this["Position"]}.json").writeFileByte(toPrettyPrintingJson().toByteArray())
            }
        }
    }

    @EventListenerHandler
    fun registerPlayerLeaveEvent(playerLeaveEvent: PlayerLeaveEvent) {
        val player = playerLeaveEvent.player
        if (Data.configServer.oneAdmin && player.isAdmin && player.autoAdmin && playerLeaveEvent.gameModule.room.playerManage.playerGroup.size > 0) {
            playerLeaveEvent.gameModule.room.playerManage.playerGroup.eachFind({ !it.isAdmin && !it.isAi && !Data.neverEnd }) {
                it.isAdmin = true
                it.autoAdmin = true
                player.isAdmin = false
                player.autoAdmin = false
                playerLeaveEvent.gameModule.room.call.sendSystemMessage("give.ok", it.name)
            }
        }

        Data.core.admin.playerDataCache[player.connectHexID] = PlayerInfo(player.connectHexID, player.kickTime, player.muteTime)

        if (playerLeaveEvent.gameModule.room.isStartGame) {
            playerLeaveEvent.gameModule.room.call.sendSystemMessage("player.dis", player.name)
        } else {
            playerLeaveEvent.gameModule.room.call.sendSystemMessage("player.disNoStart", player.name)
        }
        Log.clog("&c" + Data.i18NBundle.getinput("player.dis", player.name))

        if (Data.configServer.autoStartMinPlayerSize != -1 && playerLeaveEvent.gameModule.room.playerManage.playerGroup.size <= Data.configServer.autoStartMinPlayerSize && Threads.containsTimeTask(
                    CallTimeTask.AutoStartTask
            )) {
            Threads.closeTimeTask(CallTimeTask.AutoStartTask)
        }
        if (Data.configServer.saveRePlayFile) {
            FileUtils.getFolder(Data.ServerPlayerPath).toFile("${playerLeaveEvent.player.position}.json").delete()
        }
    }

    @EventListenerHandler
    fun registerGameStartEvent(serverGameStartEvent: ServerGameStartEvent) {
        Data.core.admin.playerDataCache.clear()

        if (Data.configServer.startAd.isNotBlank()) {
            serverGameStartEvent.gameModule.room.call.sendSystemMessage(Data.configServer.startAd)
        }

        if (Data.configServer.saveRePlayFile) {
            serverGameStartEvent.gameModule.room.playerData = Seq<Map<String, Any>>().apply {
                serverGameStartEvent.gameModule.room.playerManage.playerAll.eachAll {
                    if (!it.isAi) {
                        add(it.infoObject)
                    }
                }
            }
        }

        Log.clog("[Start New Game]")
    }

    @EventListenerHandler
    fun registerGameOverEvent(serverGameOverEvent: ServerGameOverEvent) {
        CompressionEncoderUtils.zipStream().apply {
            serverGameOverEvent.gameModule.room.playerData!!.eachAll {
                addCompressBytes("${it["Position"]}.json", it.toPrettyPrintingJson().toByteArray())
            }
        }.run {
            FileUtils.getFolder(Data.ServerPlayerPath).toFile("${getFileNameNoSuffix(serverGameOverEvent.gameModule.room.replayFileName)}.zip").writeFileByte(this.flash())
        }
        System.gc()
    }

    @EventListenerHandler
    fun registerPlayerBanEvent(serverBanEvent: PlayerBanEvent) {
        val player = serverBanEvent.player
        Data.core.admin.bannedUUIDs.add(player.connectHexID)
        Data.core.admin.bannedIPs.add(player.con!!.coverConnect().ip)
        try {
            player.kickPlayer(player.i18NBundle.getinput("kick.ban"))
        } catch (ioException: IOException) {
            error("[Player] Send Kick Player Error", ioException)
        }
        serverBanEvent.gameModule.room.call.sendSystemMessage("ban.yes", player.name)
    }

    @EventListenerHandler
    fun registerPlayerIpBanEvent(serverIpBanEvent: PlayerIpBanEvent) {
        val player = serverIpBanEvent.player
        Data.core.admin.bannedIPs.add(player.con!!.coverConnect().ip)
        try {
            player.kickPlayer(player.i18NBundle.getinput("kick.ban"))
        } catch (ioException: IOException) {
            error("[Player] Send Kick Player Error", ioException)
        }
        serverIpBanEvent.gameModule.room.call.sendSystemMessage("ban.yes", player.name)
    }
}