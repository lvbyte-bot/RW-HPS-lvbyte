/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.beta

import net.rwhps.server.data.global.Data
import net.rwhps.server.data.totalizer.TimeAndNumber
import net.rwhps.server.game.event.EventGlobalManage
import net.rwhps.server.game.event.core.EventListenerHost
import net.rwhps.server.game.event.global.NetConnectNewEvent
import net.rwhps.server.io.GameOutputStream
import net.rwhps.server.io.packet.type.PacketType
import net.rwhps.server.plugin.Plugin
import net.rwhps.server.struct.map.ObjectMap
import net.rwhps.server.util.annotations.core.EventListenerHandler
import net.rwhps.server.util.log.Log

/**
 * @date 2023/7/21 10:45
 * @author Dr (dr@der.kim)
 */
class ConnectLimit: Plugin() {
    override fun registerGlobalEvents(eventManage: EventGlobalManage) {
        eventManage.registerListener(FuckEvent())
    }

    private class FuckEvent: EventListenerHost {
        private val data = ObjectMap<String, TimeAndNumberX>(true)

        @EventListenerHandler
        fun limitConnect(netConnectNewEvent: NetConnectNewEvent) {
            if (netConnectNewEvent.connectionAgreement.ip == "127.0.0.1" ||
                netConnectNewEvent.connectionAgreement.ip.startsWith("192.168.")) {
                return
            }

            // 1 Min 只能 10 次, 多了即断开但不 BAN (6s/1)
            val limit = data[netConnectNewEvent.connectionAgreement.ip, { TimeAndNumberX(60, 10) }]
            // 24 Hour 内连接次数过 400 次, 即 BAN (144s/1)

            if (limit.countLimit.checkStatus()) {
                limit.countLimit.count++
            } else {
                val o = GameOutputStream()
                o.writeString("[CMM-D]")
                netConnectNewEvent.connectionAgreement.send(o.createPacket(PacketType.KICK))
                if (!Data.core.admin.bannedIP24.contains(netConnectNewEvent.connectionAgreement.ipLong24)) {
                    Data.core.admin.bannedIP24.add(netConnectNewEvent.connectionAgreement.ipLong24)
                    Log.clog("Auto Ban : ${netConnectNewEvent.connectionAgreement.ipLong24}")
                }

                netConnectNewEvent.connectionAgreement.close(null)
            }

            if (!limit.checkStatus()) {
                val o = GameOutputStream()
                o.writeString("[CMM-M]")
                netConnectNewEvent.connectionAgreement.send(o.createPacket(PacketType.KICK))
                netConnectNewEvent.connectionAgreement.close(null)
            } else {
                limit.count++
            }
        }
    }

    private class TimeAndNumberX(timeOut: Int, conutMax: Int): TimeAndNumber(timeOut, conutMax) {
        var countLimit = TimeAndNumber(86400, 400)
        var countLimitB = TimeAndNumber(60, 5)
    }
}