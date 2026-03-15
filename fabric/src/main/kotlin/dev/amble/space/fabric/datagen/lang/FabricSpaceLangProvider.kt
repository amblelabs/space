package dev.amble.space.fabric.datagen.lang

import dev.amble.lib.fabric.datagen.FabricAmbleLangProvider
import dev.amble.space.api.SpaceAPI
import dev.amble.space.common.lib.SpaceItems
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.core.HolderLookup
import net.minecraft.resources.ResourceLocation
import java.util.Locale
import java.util.concurrent.CompletableFuture

class FabricSpaceLangProvider {
    class EnUs(
        dataOutput: FabricDataOutput,
        registryLookup: CompletableFuture<HolderLookup.Provider>
    ) : FabricAmbleLangProvider(dataOutput, "en_us", registryLookup) {

        override fun generateTranslations(provider: HolderLookup.Provider, builder: TranslationBuilder) {
            builder.add("itemGroup.${SpaceAPI.MOD_ID}.main", "Space")

            val itemIds = mutableListOf<ResourceLocation>()
            SpaceItems.forEachItemId(itemIds::add)
            itemIds.sortedBy { it.path }.forEach { id ->
                builder.add("item.${id.namespace}.${id.path}", id.path.toEnglishDisplayName())
            }

            builder.add("key.categories.space", "Space")
            builder.add("widget.${SpaceAPI.MOD_ID}.empty", "Empty")
            builder.add("widget.${SpaceAPI.MOD_ID}.empty.desc", "...")
        }

        private fun String.toEnglishDisplayName(): String =
            split('_', '-')
                .filter(String::isNotBlank)
                .joinToString(" ") { part ->
                    part.replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString()
                    }
                }
    }
}

