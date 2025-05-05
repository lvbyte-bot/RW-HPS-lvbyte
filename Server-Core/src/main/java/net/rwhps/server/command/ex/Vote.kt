/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.command.ex

import net.rwhps.server.core.thread.CallTimeTask
import net.rwhps.server.core.thread.Threads
import net.rwhps.server.core.thread.Threads.newTimedTask
import net.rwhps.server.data.global.Data
import net.rwhps.server.game.manage.HeadlessModuleManage
import net.rwhps.server.game.player.PlayerHess
import net.rwhps.server.struct.list.Seq
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.log.exp.ImplementedException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil

//X86SMbj6UtmfhKpr6itHpUz

/**
 * Vote 为游戏提供一个默认的Vote接口
 * @author Dr (dr@der.kim)
 * @Date 2022/02/04 15:00:05
 */
class Vote {
    private val command: String
    val player: PlayerHess
    val targetPlayer: PlayerHess?
    private var isTeam: Boolean = false

    private var require: Int = 0
    private var pass: Int = 0

    private var endNoMsg: () -> Unit = {}
    private var endYesMsg: () -> Unit = {}
    private var votePlayerIng: () -> Unit = {}
    private var voteIng: () -> Unit = {}

    private var reciprocal: Int = 60

    private val playerList = Seq<String>()

    // Gameover Suss~
    constructor(command: String, hostPlayer: PlayerHess) {
        this.command = command
        this.player = hostPlayer
        this.targetPlayer = null
        preprocessing()
    }

    // Kick Other
    constructor(command: String, hostPlayer: PlayerHess, targetPlayer: PlayerHess) {
        this.command = command
        this.player = hostPlayer
        this.targetPlayer = targetPlayer
        preprocessing()
    }

    fun toVote(votePlayer: PlayerHess, playerPick: String) {
        if (playerList.contains(votePlayer.connectHexID)) {
            votePlayer.sendSystemMessage(votePlayer.i18NBundle.getinput("vote.rey"))
            return
        }

        val accapt = "y"
        val noAccapt = "n"

        if (accapt == playerPick) {
            if (isTeam) {
                if (votePlayer.team == player.team) {
                    this.pass++
                    playerList.add(votePlayer.connectHexID)
                    votePlayer.sendSystemMessage(votePlayer.i18NBundle.getinput("vote.y"))
                    votePlayerIng()
                } else {
                    votePlayer.sendSystemMessage(votePlayer.i18NBundle.getinput("vote.team"))
                }
            } else {
                this.pass++
                playerList.add(votePlayer.connectHexID)
                votePlayer.sendSystemMessage(votePlayer.i18NBundle.getinput("vote.y"))
                votePlayerIng()
            }
        } else if (noAccapt == playerPick) {
            if (isTeam) {
                if (votePlayer.team == player.team) {
                    playerList.add(votePlayer.connectHexID)
                    votePlayer.sendSystemMessage(votePlayer.i18NBundle.getinput("vote.n"))
                } else {
                    votePlayer.sendSystemMessage(votePlayer.i18NBundle.getinput("vote.team"))
                }
            } else {
                playerList.add(votePlayer.connectHexID)
                votePlayer.sendSystemMessage(votePlayer.i18NBundle.getinput("vote.n"))
            }
        }
        inspectEnd()
    }

    private fun preprocessing() {
        // 预处理
        when {
            commandStartData.contains(command) -> commandStartData[command]!!.invoke(this)
            else -> {
                player.sendSystemMessage(player.i18NBundle.getinput("vote.end.err", command))
                clearUp()
            }
        }
    }

    /**
     * 正常投票
     */
    private fun normalDistribution() {
        require = HeadlessModuleManage.hps.room.playerManage.playerGroup.size
        endNoMsg = {
            HeadlessModuleManage.hps.room.call.sendSystemMessageLocal(
                    "vote.done.no", command + " " + (targetPlayer?.name ?: ""), pass, this.require
            )
        }
        endYesMsg = { HeadlessModuleManage.hps.room.call.sendSystemMessageLocal("vote.ok") }
        votePlayerIng = { HeadlessModuleManage.hps.room.call.sendSystemMessage("vote.y.ing", command, pass, this.require) }
        voteIng = { HeadlessModuleManage.hps.room.call.sendSystemMessage("vote.ing", reciprocal) }
        start { HeadlessModuleManage.hps.room.call.sendSystemMessage("vote.start", player.name, command + " " + (targetPlayer?.name ?: "")) }
    }

    /**
     * 团队投票
     */
    private fun teamOnly() {
        val require = AtomicInteger(0)
        HeadlessModuleManage.hps.room.playerManage.playerGroup.eachAllFind({ e: PlayerHess -> e.team == player.team }) { _: PlayerHess -> require.getAndIncrement() }
        this.require = require.get()
        endNoMsg = {
            HeadlessModuleManage.hps.room.call.sendSystemTeamMessageLocal(
                    player.team, "vote.done.no", command + " " + (targetPlayer?.name ?: ""), pass, this.require
            )
        }
        endYesMsg = { HeadlessModuleManage.hps.room.call.sendSystemTeamMessageLocal(player.team, "vote.ok") }
        votePlayerIng = {
            HeadlessModuleManage.hps.room.call.sendSystemTeamMessageLocal(
                    player.team, "vote.y.ing", command, pass, this.require
            )
        }
        voteIng = { HeadlessModuleManage.hps.room.call.sendSystemTeamMessageLocal(player.team, "vote.ing", reciprocal) }
        start {
            HeadlessModuleManage.hps.room.call.sendSystemTeamMessageLocal(
                    player.team, "vote.start", player.name, command + " " + (targetPlayer?.name ?: "")
            )
        }
    }

    private fun start(run: () -> Unit) {
        val temp = require
        require = if (temp <= 1) {
            player.sendSystemMessage("vote.no1")
            1
        } else if (temp <= 3) {
            2
        } else {
            ceil(temp.toDouble() / 2).toInt()
        }

        playerList.add(player.connectHexID)

        pass++

        if (pass >= require) {
            end()
        } else {
            newTimedTask(CallTimeTask.VoteTask, 10, 10, TimeUnit.SECONDS) {
                this.reciprocal -= 10
                voteIng()
                if (this.reciprocal <= 0) {
                    end()
                    Threads.closeTimeTask(CallTimeTask.VoteTask)
                }
            }
            run()
        }
    }

    private fun inspectEnd() {
        if (this.pass >= this.require) {
            end()
        }
    }

    private fun end() {
        var error: ImplementedException.VoteImplementedException? = null
        if (this.pass >= this.require) {
            endYesMsg()
            when {
                commandEndData.contains(command) -> commandEndData[command]!!.invoke(this)
                else -> {
                    error = ImplementedException.VoteImplementedException("[Vote End] Server does not implement command: $command")
                    clearUp()
                }
            }
        } else {
            endNoMsg()
        }
        clearUp()
        if (error != null) {
            Log.error(error)
        }
    }

    /**
     * 清理引用
     */
    private fun clearUp() {
        Threads.closeTimeTask(CallTimeTask.VoteTask)

        playerList.clear()

        val nullVal: () -> Unit = {}
        endNoMsg = nullVal
        endYesMsg = nullVal
        votePlayerIng = nullVal
        voteIng = nullVal

        pass = 0
        reciprocal = 60

        Data.vote = null
    }

    fun stopVote() {
        clearUp()
    }

    companion object {
        private val commandStartData = mutableMapOf<String, (vote: Vote) -> Unit>()
        private val commandEndData = mutableMapOf<String, (vote: Vote) -> Unit>()

        init {
            commandStartData["gameover"] = { it.normalDistribution() }

            commandEndData["gameover"] = { HeadlessModuleManage.hps.room.gr() }
        }

        @JvmStatic
        fun addVoteFullParticipation(command: String, run: (vote: Vote) -> Unit): Boolean {
            return if (commandStartData.contains(command)) {
                false
            } else {
                commandStartData[command] = { run(it); it.normalDistribution() }
                true
            }
        }

        @JvmStatic
        fun addVoteTeamOnly(command: String, run: (vote: Vote) -> Unit): Boolean {
            return if (commandStartData.contains(command)) {
                false
            } else {
                commandStartData[command] = { run(it); it.isTeam = true; it.teamOnly() }
                true
            }
        }
    }
}