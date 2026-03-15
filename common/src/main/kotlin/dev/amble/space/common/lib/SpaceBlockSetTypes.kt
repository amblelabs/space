package dev.amble.space.common.lib

import net.minecraft.world.level.block.state.properties.BlockSetType
import java.util.function.Consumer

@Suppress("unused")
object SpaceBlockSetTypes {
    private val TYPES = mutableListOf<BlockSetType>()

    @JvmStatic
    fun registerBlocks(r: Consumer<BlockSetType>) = TYPES.forEach(r::accept)

    private fun register(type: BlockSetType): BlockSetType = type.also { TYPES.add(it) }
}

