package dev.amble.space.fabric.datagen.tag

import dev.amble.lib.fabric.datagen.FabricAmbleBlockTagProvider
import dev.amble.space.xplat.IXplatTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture

class SpaceBlockTagProvider(
    output: FabricDataOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider>,
    @Suppress("unused") private val xtags: IXplatTags
) : FabricAmbleBlockTagProvider(output, lookupProvider) {

    override fun addTags(provider: HolderLookup.Provider) {
        // add block tags here
    }
}

