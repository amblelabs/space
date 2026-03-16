package dev.amble.space.forge

import dev.amble.space.api.SpaceAPI
import dev.amble.space.client.SpaceKeybinds
import dev.amble.space.client.model.SpaceModelLayers
import dev.amble.space.common.lib.SpaceParticles
import dev.amble.space.forge.client.RegisterClientStuff
import dev.amble.space.forge.xplat.ForgeClientXplatImpl
import dev.amble.space.interop.ClientSpaceInterop
import dev.amble.space.interop.SpaceInterop
import dev.amble.space.xplat.IClientXplatAbstractions
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent
import java.util.function.BiConsumer

@EventBusSubscriber(modid = SpaceAPI.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
object ForgeSpaceClient {
    @SubscribeEvent
    fun onClientSetup(event: FMLClientSetupEvent) {
        RegisterClientStuff.init()
        IClientXplatAbstractions.INSTANCE.initPlatformSpecific()
        ForgeClientXplatImpl.flushClientSetupWork(event)
        ClientSpaceInterop.init()
    }

    @SubscribeEvent
    fun onRegisterKeyMappings(event: RegisterKeyMappingsEvent) {
        SpaceKeybinds.all().forEach { event.register(it) }
    }

    @SubscribeEvent
    fun onRegisterLayerDefinitions(event: EntityRenderersEvent.RegisterLayerDefinitions) {
        SpaceModelLayers.init(BiConsumer { location, definition -> event.registerLayerDefinition(location, definition::get) })
    }

    @SubscribeEvent
    fun onRegisterParticleProviders(event: RegisterParticleProvidersEvent) {
        SpaceParticles.FactoryHandler.registerFactories<Nothing> { type, constructor ->
            event.registerSpriteSet(type, constructor::apply)
        }
    }

    @SubscribeEvent
    fun onRegisterRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        RegisterClientStuff.registerBlockEntityRenderers(object : RegisterClientStuff.BlockEntityRendererRegisterer {
            override fun <T : net.minecraft.world.level.block.entity.BlockEntity> registerBlockEntityRenderer(
                type: net.minecraft.world.level.block.entity.BlockEntityType<T>,
                provider: net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider<in T>
            ) {
                @Suppress("UNCHECKED_CAST")
                event.registerBlockEntityRenderer(type, provider as net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider<T>)
            }
        })

        ForgeClientXplatImpl.flushEntityRendererRegistrations(event)
    }

    @SubscribeEvent
    fun onRegisterItemColors(event: RegisterColorHandlersEvent.Item) {
        RegisterClientStuff.registerColorProviders(
            { color, item -> event.register(color, item) },
            { color, block -> event.blockColors.register(color, block) }
        )
    }

    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent.Post) {
        ClientSpaceInterop.tick()
    }
}

