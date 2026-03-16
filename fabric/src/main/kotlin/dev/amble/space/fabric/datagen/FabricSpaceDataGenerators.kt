package dev.amble.space.fabric.datagen

import dev.amble.space.api.SpaceAPI
import dev.amble.space.datagen.IXplatIngredients
import dev.amble.space.datagen.SpaceAdvancements
import dev.amble.space.datagen.SpaceLootTables
import dev.amble.space.datagen.recipe.SpaceXplatRecipes
import dev.amble.space.fabric.datagen.lang.FabricSpaceLangProvider
import dev.amble.space.fabric.datagen.tag.SpaceBlockTagProvider
import dev.amble.space.fabric.datagen.tag.SpaceItemTagProvider
import dev.amble.space.xplat.IXplatAbstractions
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.core.registries.Registries
import net.minecraft.data.advancements.AdvancementProvider
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets

@Suppress("unused")
class FabricSpaceDataGenerators : DataGeneratorEntrypoint {

    override fun onInitializeDataGenerator(gen: FabricDataGenerator) {
        val pack = gen.createPack()
        val tags = IXplatAbstractions.INSTANCE.tags()

        pack.addProvider { output, lookup -> SpaceXplatRecipes(output, lookup, INGREDIENTS) }

        val blockTags = BlockTagWrapper()
        pack.addProvider { output, lookup ->
            SpaceBlockTagProvider(output, lookup, tags).also { blockTags.provider = it }
        }
        pack.addProvider { output, lookup -> SpaceItemTagProvider(output, lookup, blockTags.provider, tags) }

        pack.addProvider { output, lookup ->
            LootTableProvider(
                output,
                setOf(),
                listOf(LootTableProvider.SubProviderEntry({ SpaceLootTables() }, LootContextParamSets.ALL_PARAMS)),
                lookup
            )
        }

        pack.addProvider { output, lookup -> AdvancementProvider(output, lookup, listOf(SpaceAdvancements())) }
        pack.addProvider { output, _ -> FabricSpaceModelProvider(output) }
        pack.addProvider(FabricSpaceLangProvider::EnUs)
        pack.addProvider { output, _ -> FabricSoundProvider(output, SpaceAPI.MOD_ID)}
    }

    private class BlockTagWrapper {
        lateinit var provider: SpaceBlockTagProvider
    }

    companion object {
        private val INGREDIENTS = object : IXplatIngredients {}

        @Suppress("unused")
        private fun tag(s: String): TagKey<Item> = tag("c", s)

        private fun tag(namespace: String, s: String): TagKey<Item> =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, s))
    }
}
