package dev.amble.space.common.blocks.behavior

import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.ComposterBlock

object SpaceComposting {
    @JvmStatic
    fun setup() {
        // register compostable items here
    }

    @Suppress("unused")
    private fun compost(itemLike: ItemLike, chance: Float) {
        val item = itemLike.asItem()
        if (item != Items.AIR) ComposterBlock.COMPOSTABLES[item] = chance
    }
}

