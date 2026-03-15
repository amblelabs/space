package dev.amble.space.fabric.datagen.lang

import dev.amble.lib.fabric.datagen.FabricAmbleLangProvider
import dev.amble.space.api.SpaceAPI
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture

class FabricSpaceLangProvider {
    class EnUs(
        dataOutput: FabricDataOutput,
        registryLookup: CompletableFuture<HolderLookup.Provider>
    ) : FabricAmbleLangProvider(dataOutput, "en_us", registryLookup) {

        override fun generateTranslations(provider: HolderLookup.Provider, builder: TranslationBuilder) {
            builder.add("itemGroup.${SpaceAPI.MOD_ID}.main", "Space")
            builder.add("key.categories.space", "Space")
            builder.add("widget.${SpaceAPI.MOD_ID}.empty", "Empty")
            builder.add("widget.${SpaceAPI.MOD_ID}.empty.desc", "...")
        }
    }
}

