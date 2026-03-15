package dev.amble.space.common.blocks.behavior

import net.minecraft.world.level.block.Block

object SpaceStrippable {
    @JvmField
    val STRIPPABLE = hashMapOf<Block, Block>()

    @JvmStatic
    fun init() {
        // register strippable block pairs here
    }
}

