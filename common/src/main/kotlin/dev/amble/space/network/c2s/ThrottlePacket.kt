package dev.amble.space.network.c2s

import dev.amble.space.api.SpaceAPI
import dev.amble.space.common.entity.RocketContraptionEntity
import dev.amble.space.network.SpacePackets
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class ThrottlePacket(val throttle: Float) : CustomPacketPayload {
    override fun type() = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<ThrottlePacket>(
            SpaceAPI.modLoc("throttle")
        )
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ThrottlePacket> =
            StreamCodec.ofMember(
                { packet, buf -> buf.writeFloat(packet.throttle) },
                { buf -> ThrottlePacket(buf.readFloat()) }
            )

        fun handle(packet: ThrottlePacket, ctx: SpacePackets.PacketContext) {
            ctx.enqueue {
                val player = ctx.player() ?: return@enqueue
                val rocket = player.vehicle as? RocketContraptionEntity ?: return@enqueue
                rocket.entityData.set(RocketContraptionEntity.THROTTLE, packet.throttle)
            }
        }
    }
}