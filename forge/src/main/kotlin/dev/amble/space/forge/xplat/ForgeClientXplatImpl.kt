package dev.amble.space.forge.xplat

import dev.amble.space.xplat.IClientXplatAbstractions
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.item.ItemProperties
import net.minecraft.client.renderer.item.ItemPropertyFunction
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.network.PacketDistributor

class ForgeClientXplatImpl : IClientXplatAbstractions {
    override fun sendPacketToServer(packet: CustomPacketPayload) = PacketDistributor.sendToServer(packet)

    override fun setRenderLayer(block: Block, type: RenderType) {
        synchronized(CLIENT_SETUP_WORK) {
            CLIENT_SETUP_WORK += { ItemBlockRenderTypes.setRenderLayer(block, type) }
        }
    }

    override fun initPlatformSpecific() {
        // add optional client mod interop here
    }

    override fun <T : Entity> registerEntityRenderer(type: EntityType<out T>, renderer: EntityRendererProvider<T>) {
        synchronized(ENTITY_RENDERER_REGISTRATIONS) {
            ENTITY_RENDERER_REGISTRATIONS += { event -> event.registerEntityRenderer(type, renderer) }
        }
    }

    @Suppress("DEPRECATION")
    override fun registerItemProperty(item: Item, id: ResourceLocation, func: ItemPropertyFunction) {
        synchronized(CLIENT_SETUP_WORK) {
            CLIENT_SETUP_WORK += { ItemProperties.register(item, id, func) }
        }
    }

    companion object {
        private val CLIENT_SETUP_WORK = mutableListOf<() -> Unit>()
        private val ENTITY_RENDERER_REGISTRATIONS = mutableListOf<(EntityRenderersEvent.RegisterRenderers) -> Unit>()

        @JvmStatic
        fun flushClientSetupWork(event: FMLClientSetupEvent) {
            val work = synchronized(CLIENT_SETUP_WORK) {
                CLIENT_SETUP_WORK.toList().also { CLIENT_SETUP_WORK.clear() }
            }

            event.enqueueWork {
                work.forEach { it.invoke() }
            }
        }

        @JvmStatic
        fun flushEntityRendererRegistrations(event: EntityRenderersEvent.RegisterRenderers) {
            val registrations = synchronized(ENTITY_RENDERER_REGISTRATIONS) {
                ENTITY_RENDERER_REGISTRATIONS.toList().also { ENTITY_RENDERER_REGISTRATIONS.clear() }
            }

            registrations.forEach { it.invoke(event) }
        }
    }
}

