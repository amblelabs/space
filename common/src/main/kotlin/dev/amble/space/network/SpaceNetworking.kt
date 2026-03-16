package dev.amble.space.network

import dev.amble.space.network.c2s.ThrottlePacket
import dev.amble.space.network.c2s.RocketControlPacket
import dev.amble.space.network.c2s.ForceDismountPacket
import dev.amble.space.network.s2c.RocketBlocksSyncPacket

object SpaceNetworking {
    fun registerPackets() {
        SpacePackets.registerS2C(
            RocketBlocksSyncPacket.TYPE,
            RocketBlocksSyncPacket.STREAM_CODEC,
            RocketBlocksSyncPacket::handle
        )

        SpacePackets.registerC2S(
            ThrottlePacket.TYPE,
            ThrottlePacket.STREAM_CODEC,
            ThrottlePacket::handle
        )

        SpacePackets.registerC2S(
            RocketControlPacket.TYPE,
            RocketControlPacket.STREAM_CODEC,
            RocketControlPacket::handle
        )

        SpacePackets.registerC2S(
            ForceDismountPacket.TYPE,
            ForceDismountPacket.STREAM_CODEC,
            ForceDismountPacket::handle
        )
    }
}