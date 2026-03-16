package dev.amble.space.network.s2c

import dev.amble.space.api.SpaceAPI
import dev.amble.space.api.planet.client.ClientSolarSystem
import dev.amble.space.network.SpacePackets
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class SolarSystemSyncPacket(
    val elapsedSeconds: Double,
    val timeScale: Double
) : CustomPacketPayload {

    override fun type() = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<SolarSystemSyncPacket>(
            SpaceAPI.modLoc("solar_system_sync")
        )

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SolarSystemSyncPacket> =
            StreamCodec.ofMember(
                { packet, buf ->
                    buf.writeDouble(packet.elapsedSeconds)
                    buf.writeDouble(packet.timeScale)
                },
                { buf ->
                    SolarSystemSyncPacket(
                        elapsedSeconds = buf.readDouble(),
                        timeScale = buf.readDouble()
                    )
                }
            )

        fun handle(packet: SolarSystemSyncPacket, ctx: SpacePackets.PacketContext) {
            ctx.enqueue {
                ClientSolarSystem.onSync(packet.elapsedSeconds, packet.timeScale)
            }
        }
    }
}