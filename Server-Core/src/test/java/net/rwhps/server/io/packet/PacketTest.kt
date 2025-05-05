/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.io.packet

import net.rwhps.server.io.packet.type.PacketType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PacketTest {

    @Test
    fun newTest() {
        val bytes = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        val packet = Packet(PacketType.EMPTYP_ACKAGE, bytes)

        assertEquals(packet.type, PacketType.EMPTYP_ACKAGE) { "[PacketTest] Type Error" }
        assertEquals(packet.bytes, bytes) { "[PacketTest] Bytes Error" }

        packet.toString()
    }
}