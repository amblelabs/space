package dev.amble.space.network.c2s

import dev.amble.space.api.SpaceAPI
import dev.amble.space.common.entity.RocketContraptionEntity
import dev.amble.space.network.SpacePackets
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class RocketControlPacket(
    val pitchInput: Float,
    val yawInput: Float,
    val throttleUp: Boolean,
    val throttleDown: Boolean
) : CustomPacketPayload {
    override fun type() = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<RocketControlPacket>(
            SpaceAPI.modLoc("rocket_control")
        )
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, RocketControlPacket> =
            StreamCodec.ofMember(
                { packet, buf ->
                    buf.writeFloat(packet.pitchInput)
                    buf.writeFloat(packet.yawInput)
                    buf.writeBoolean(packet.throttleUp)
                    buf.writeBoolean(packet.throttleDown)
                },
                { buf ->
                    RocketControlPacket(
                        pitchInput = buf.readFloat(),
                        yawInput = buf.readFloat(),
                        throttleUp = buf.readBoolean(),
                        throttleDown = buf.readBoolean()
                    )
                }
            )

        fun handle(packet: RocketControlPacket, ctx: SpacePackets.PacketContext) {
            ctx.enqueue {
                val player = ctx.player() ?: return@enqueue
                val rocket = player.vehicle as? RocketContraptionEntity ?: return@enqueue
                rocket.pitchInput = packet.pitchInput.coerceIn(-1f, 1f)
                rocket.yawInput = packet.yawInput.coerceIn(-1f, 1f)
                rocket.throttleUp = packet.throttleUp
                rocket.throttleDown = packet.throttleDown
            }
        }
    }
}

