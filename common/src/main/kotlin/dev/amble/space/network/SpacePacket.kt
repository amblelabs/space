package dev.amble.space.network

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

interface SpacePacket<T : SpacePacket<T>> : CustomPacketPayload {
    val type: CustomPacketPayload.Type<T>
    val streamCodec: StreamCodec<RegistryFriendlyByteBuf, T>

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> = type
}