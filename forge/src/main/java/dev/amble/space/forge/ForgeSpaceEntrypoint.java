package dev.amble.space.forge;

import dev.amble.space.api.SpaceAPI;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(SpaceAPI.MOD_ID)
public final class ForgeSpaceEntrypoint {
    public ForgeSpaceEntrypoint(IEventBus modBus, ModContainer ignoredModContainer) {
        ForgeSpace.INSTANCE.init(modBus);
    }
}



