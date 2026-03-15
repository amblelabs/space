package dev.amble.space.datagen

import dev.amble.lib.datagen.AmbleLootTableSubProvider
import dev.amble.space.api.SpaceAPI
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootTable

@Suppress("unused")
class SpaceLootTables : AmbleLootTableSubProvider(SpaceAPI.MOD_ID) {
    override fun makeLootTables(
        blockTables: MutableMap<Block, LootTable.Builder>,
        lootTables: MutableMap<ResourceKey<LootTable>, LootTable.Builder>
    ) {
        
    }
}

