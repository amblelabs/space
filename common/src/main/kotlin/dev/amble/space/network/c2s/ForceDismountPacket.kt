package dev.amble.space.network.c2s

import dev.amble.space.api.SpaceAPI
import dev.amble.space.common.entity.RocketContraptionEntity
import dev.amble.space.network.SpacePackets
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data object ForceDismountPacket : CustomPacketPayload {
    override fun type() = TYPE

    val TYPE = CustomPacketPayload.Type<ForceDismountPacket>(
        SpaceAPI.modLoc("force_dismount")
    )

    val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ForceDismountPacket> =
        StreamCodec.unit(ForceDismountPacket)

    fun handle(@Suppress("UNUSED_PARAMETER") packet: ForceDismountPacket, ctx: SpacePackets.PacketContext) {
        ctx.enqueue {
            val player = ctx.player() ?: return@enqueue
            val rocket = player.vehicle as? RocketContraptionEntity ?: return@enqueue
            rocket.forceDismount(player)
        }
    }
}


