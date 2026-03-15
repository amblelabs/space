package dev.amble.space.fabric.datagen

import dev.amble.lib.fabric.datagen.FabricAmbleModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.data.models.ItemModelGenerators

class FabricSpaceModelProvider(output: FabricDataOutput) : FabricAmbleModelProvider(output) {
    override fun generateBlockStateModels(gen: BlockModelGenerators) {}
    override fun generateItemModels(gen: ItemModelGenerators) {}
}

