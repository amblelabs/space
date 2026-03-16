package dev.amble.space.common.blocks.rocket

import dev.amble.space.common.contraption.RocketStructure
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class RocketEngineBlock(properties: BlockBehaviour.Properties) : Block(properties), RocketComponentBlock {

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS

        // try to validate and launch rocket on right click
        val structure = RocketStructure.validate(level, pos)
        if (structure == null) {
            player.displayClientMessage(
                Component.translatable("block.space.rocket_engine.invalid"),
                true
            )
            return InteractionResult.FAIL
        }

        player.displayClientMessage(
            Component.translatable("block.space.rocket_engine.valid", structure.blocks.size),
            true
        )
        return InteractionResult.SUCCESS
    }
}