package dev.amble.space.fabric.datagen.tag

import dev.amble.lib.fabric.datagen.FabricAmbleItemTagProvider
import dev.amble.space.xplat.IXplatTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture

class SpaceItemTagProvider(
    output: FabricDataOutput,
    completableFuture: CompletableFuture<HolderLookup.Provider>,
    blockTagProvider: FabricTagProvider.BlockTagProvider?,
    @Suppress("unused") private val xtags: IXplatTags
) : FabricAmbleItemTagProvider(output, completableFuture, blockTagProvider) {

    override fun addTags(provider: HolderLookup.Provider) {
        // add item tags here
    }
}
