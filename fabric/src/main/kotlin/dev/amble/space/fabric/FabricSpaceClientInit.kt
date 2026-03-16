package dev.amble.space.fabric

import dev.amble.space.client.SpaceKeybinds
import dev.amble.space.client.model.SpaceModelLayers
import dev.amble.space.common.lib.SpaceParticles
import dev.amble.space.fabric.client.RegisterClientStuff
import dev.amble.space.interop.ClientSpaceInterop
import dev.amble.space.interop.SpaceInterop
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry

class FabricSpaceClientInit : ClientModInitializer {
    override fun onInitializeClient() {
        RegisterClientStuff.init()

        SpaceKeybinds.all().forEach { KeyBindingHelper.registerKeyBinding(it) }

        SpaceModelLayers.init { loc, def -> EntityModelLayerRegistry.registerModelLayer(loc, def::get) }

        SpaceParticles.FactoryHandler.registerFactories<Nothing> { type, constructor ->
            ParticleFactoryRegistry.getInstance().register(type, constructor::apply)
        }

        ClientSpaceInterop.init()

        ClientTickEvents.END_CLIENT_TICK.register { mc -> ClientSpaceInterop.tick() }
    }
}


