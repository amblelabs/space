package dev.amble.space.forge

import dev.amble.space.api.SpaceAPI
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge

@Mod(SpaceAPI.MOD_ID)
class ForgeSpace(modEventBus: IEventBus, modContainer: ModContainer) {
    init {
        NeoForge.EVENT_BUS.register(this)
    }
}

