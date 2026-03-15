package dev.amble.space.fabric.datagen.tag

import dev.amble.lib.datagen.PickaxeMineable
import dev.amble.lib.fabric.datagen.FabricAmbleBlockTagProvider
import dev.amble.lib.reflection.ReflectionUtil
import dev.amble.space.common.lib.SpaceBlocks
import dev.amble.space.xplat.IXplatTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.core.HolderLookup
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Block
import java.util.Optional
import java.util.concurrent.CompletableFuture

class SpaceBlockTagProvider(
    output: FabricDataOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider>,
    @Suppress("unused") private val xtags: IXplatTags
) : FabricAmbleBlockTagProvider(output, lookupProvider) {

    override fun addTags(provider: HolderLookup.Provider) {
        val pickaxe = getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_PICKAXE)
        val map = ReflectionUtil.getAnnotatedValues(SpaceBlocks.javaClass, Block::class.java, PickaxeMineable::class.java, false)

        map.forEach() { (block, optional) ->
            pickaxe.add(block)
            val annotation = optional.orElseThrow()
            val tag = annotation?.tool?.tag ?: return@forEach

            getOrCreateTagBuilder(tag).add(block)
        }
    }
}

