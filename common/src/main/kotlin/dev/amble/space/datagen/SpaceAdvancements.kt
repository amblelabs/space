package dev.amble.space.datagen

import dev.amble.lib.datagen.AmbleAdvancementSubProvider
import dev.amble.space.api.SpaceAPI
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.HolderLookup
import java.util.function.Consumer

class SpaceAdvancements : AmbleAdvancementSubProvider(SpaceAPI.MOD_ID) {
    override fun generate(registries: HolderLookup.Provider, saver: Consumer<AdvancementHolder>) {
        // register advancements here
    }
}

