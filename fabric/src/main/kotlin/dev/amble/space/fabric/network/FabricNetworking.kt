package dev.amble.space.fabric.network

import dev.amble.space.network.SpacePackets
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.entity.Entity
import java.util.concurrent.CompletableFuture

object FabricNetworking {
    fun register() {
        SpacePackets.allS2C().forEach { registration ->
            registerS2C(registration)
        }
        SpacePackets.allC2S().forEach {
            registerC2SHandler(it)
        }
    }

    private fun <T : CustomPacketPayload> registerC2SHandler(
        registration: SpacePackets.PacketRegistration<T>
    ) {
        PayloadTypeRegistry.playC2S().register(registration.type, registration.codec)
        ServerPlayNetworking.registerGlobalReceiver(registration.type) { packet, ctx ->
            registration.handler(packet, object : SpacePackets.PacketContext {
                override fun enqueue(block: () -> Unit): CompletableFuture<Void?> {
                    val future = CompletableFuture<Void?>()
                    ctx.server().execute {
                        try { block(); future.complete(null) }
                        catch (e: Exception) { future.completeExceptionally(e) }
                    }
                    return future
                }
                override fun player() = ctx.player()
                override fun level() = ctx.player().level()
            })
        }
    }

    private fun <T : CustomPacketPayload> registerS2C(
        registration: SpacePackets.PacketRegistration<T>
    ) {
        PayloadTypeRegistry.playS2C().register(registration.type, registration.codec)
        ClientPlayNetworking.registerGlobalReceiver(registration.type) { packet, ctx ->
            registration.handler(packet, object : SpacePackets.PacketContext {
                override fun enqueue(block: () -> Unit): CompletableFuture<Void?> {
                    val future = CompletableFuture<Void?>()
                    ctx.client().execute {
                        try {
                            block()
                            future.complete(null)
                        } catch (e: Exception) {
                            future.completeExceptionally(e)
                        }
                    }
                    return future
                }
                override fun player() = ctx.player()
                override fun level() = ctx.client().level
            })
        }
    }
}