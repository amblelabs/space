package dev.amble.space.network

import dev.amble.space.network.s2c.RocketBlocksSyncPacket
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

object SpacePackets {
    data class PacketRegistration<T : CustomPacketPayload>(
        val type: CustomPacketPayload.Type<T>,
        val codec: StreamCodec<RegistryFriendlyByteBuf, T>,
        val handler: (T, PacketContext) -> Unit
    )

    // context abstraction so handler code is platform-agnostic
    interface PacketContext {
        fun enqueue(block: () -> Unit): CompletableFuture<Void?>
        fun player(): Player?
        fun level(): Level?
    }

    private val C2S = mutableListOf<PacketRegistration<*>>()

    fun <T : CustomPacketPayload> registerC2S(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<RegistryFriendlyByteBuf, T>,
        handler: (T, PacketContext) -> Unit
    ) {
        C2S += PacketRegistration(type, codec, handler)
    }

    fun allC2S(): List<PacketRegistration<*>> = C2S

    private val S2C = mutableListOf<PacketRegistration<*>>()

    fun <T : CustomPacketPayload> registerS2C(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<RegistryFriendlyByteBuf, T>,
        handler: (T, PacketContext) -> Unit
    ) {
        S2C += PacketRegistration(type, codec, handler)
    }

    fun allS2C(): List<PacketRegistration<*>> = S2C
}