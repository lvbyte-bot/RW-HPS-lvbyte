/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.command

import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.game.manage.HeadlessModuleManage
import net.rwhps.server.game.room.RelayRoom
import net.rwhps.server.io.GameOutputStream
import net.rwhps.server.io.packet.type.PacketType
import net.rwhps.server.net.NetService
import net.rwhps.server.net.core.IRwHps.NetType.*
import net.rwhps.server.net.core.server.AbstractNetConnect
import net.rwhps.server.util.game.command.CommandHandler
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Dr (dr@der.kim)
 */
class CommandsEx(handler: CommandHandler) {
    private fun registerCore(handler: CommandHandler) {
        handler.register("hi", "") { _: Array<String>?, con: AbstractNetConnect ->
            val out = GameOutputStream()
            if (NetStaticData.RwHps.netType == NullProtocol) {
                out.writeString("SERVER CLOSE")
            } else {
                out.writeString("ServerNetType")
                out.writeString(NetStaticData.RwHps.netType.name)
            }

            when (NetStaticData.RwHps.netType) {
                ServerProtocol, ServerProtocolOld, ServerTestProtocol -> {
                    out.writeString("PlayerSize")
                    out.writeInt(HeadlessModuleManage.hps.room.playerManage.playerGroup.size)
                    out.writeString("MaxPlayer")
                    out.writeInt(Data.configServer.maxPlayer)

                    out.writeString("MapName")
                    out.writeString(HeadlessModuleManage.hps.room.maps.mapName)
                    out.writeString("Income")
                    out.writeFloat(HeadlessModuleManage.hps.gameLinkServerData.income)
                    out.writeString("Credits")
                    out.writeInt(
                            when (HeadlessModuleManage.hps.gameLinkServerData.credits) {
                                1 -> 0
                                2 -> 1000
                                3 -> 2000
                                4 -> 5000
                                5 -> 10000
                                6 -> 50000
                                7 -> 100000
                                8 -> 200000
                                0 -> 4000
                                else -> 0
                            }
                    )
                    out.writeString("NoNukes")
                    out.writeBoolean(HeadlessModuleManage.hps.gameLinkServerData.nukes)
                    out.writeString("InitUnit")
                    out.writeInt(HeadlessModuleManage.hps.gameLinkServerData.startingunits)
                    out.writeString("Mist")
                    out.writeInt(HeadlessModuleManage.hps.gameLinkServerData.fog)
                    out.writeString("SharedControl")
                    out.writeBoolean(HeadlessModuleManage.hps.gameLinkServerData.sharedcontrol)
                }
                RelayProtocol, RelayMulticastProtocol -> {
                    out.writeString("PlayerSize")
                    val size = AtomicInteger()
                    NetStaticData.netService.eachAll { e: NetService -> size.addAndGet(e.getConnectSize()) }
                    out.writeInt(size.get())

                    out.writeString("RoomAllSize")
                    out.writeInt(RelayRoom.roomAllSize)
                    out.writeString("RoomPublicListSize")
                    out.writeInt(RelayRoom.roomPublicSize)
                    out.writeString("RoomNoStartSize")
                    out.writeInt(RelayRoom.roomNoStartSize)
                }
                HttpProtocol, RemoteControlProtocol, GameProtocol -> {}
                GlobalProtocol, DedicatedToTheBackend, NullProtocol -> {}
            }
            con.sendPacket(out.createPacket(PacketType.GET_SERVER_INFO))
        }
    }

    companion object {
        private val localeUtil = Data.i18NBundle
    }

    init {
        registerCore(handler)
    }
}