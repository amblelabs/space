package dev.amble.space.forge.network

import dev.amble.space.api.SpaceAPI
import dev.amble.space.network.SpacePackets
import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.entity.Entity
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.registration.PayloadRegistrar
import java.util.concurrent.CompletableFuture

object ForgeNetworking {
    fun register(modBus: IEventBus) {
        modBus.addListener { event: RegisterPayloadHandlersEvent ->
            val registrar = event.registrar(SpaceAPI.MOD_ID)
            SpacePackets.allS2C().forEach { registration ->
                registerS2C(registrar, registration)
            }
            SpacePackets.allC2S().forEach { registerC2S(registrar, it) }
        }
    }


    private fun <T : CustomPacketPayload> registerC2S(
        registrar: PayloadRegistrar,
        registration: SpacePackets.PacketRegistration<T>
    ) {
        registrar.playToServer(
            registration.type,
            registration.codec
        ) { packet, ctx ->
            registration.handler(packet, object : SpacePackets.PacketContext {
                override fun enqueue(block: () -> Unit): CompletableFuture<Void?> =
                    ctx.enqueueWork(block).thenApply { null }
                override fun player() = ctx.player()
                override fun level() = ctx.player()?.level()
            })
        }
    }

    private fun <T : CustomPacketPayload> registerS2C(
        registrar: PayloadRegistrar,
        registration: SpacePackets.PacketRegistration<T>
    ) {
        registrar.playToClient(
            registration.type,
            registration.codec
        ) { packet, ctx ->
            registration.handler(packet, object : SpacePackets.PacketContext {
                override fun enqueue(block: () -> Unit) = ctx.enqueueWork(block)
                override fun player() = ctx.player()
                override fun level() = Minecraft.getInstance().level
            })
        }
    }
}