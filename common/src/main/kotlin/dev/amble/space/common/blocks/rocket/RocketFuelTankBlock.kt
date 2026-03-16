package dev.amble.space.common.blocks.rocket

import dev.amble.space.common.blockentity.rocket.RocketFuelTankBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState

class RocketFuelTankBlock(properties: BlockBehaviour.Properties) : Block(properties), RocketComponentBlock, EntityBlock {

    companion object {
        // fuel stored in the block entity
        const val MAX_FUEL = 1000f
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        RocketFuelTankBlockEntity(pos, state)

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL
}
