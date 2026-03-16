package dev.amble.space.common.blocks.rocket

import dev.amble.space.common.contraption.RocketStructure
import dev.amble.space.common.entity.RocketContraptionEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.phys.BlockHitResult

class RocketSeatBlock(properties: BlockBehaviour.Properties) : Block(properties), RocketComponentBlock {

    companion object {
        val FACING: DirectionProperty = BlockStateProperties.HORIZONTAL_FACING
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState =
        defaultBlockState().setValue(FACING, ctx.horizontalDirection.opposite)

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS
        if (player !is ServerPlayer) return InteractionResult.FAIL

        // find the engine block — scan nearby for it
        val enginePos = findEngine(level, pos) ?: run {
            player.displayClientMessage(
                Component.translatable("block.space.rocket_seat.no_engine"),
                true
            )
            return InteractionResult.FAIL
        }

        val structure = RocketStructure.validate(level, enginePos) ?: run {
            player.displayClientMessage(
                Component.translatable("block.space.rocket_seat.invalid"),
                true
            )
            return InteractionResult.FAIL
        }

        // convert structure to contraption and mount player
        val rocket = RocketContraptionEntity.fromStructure(structure, level as ServerLevel)
        player.startRiding(rocket)

        return InteractionResult.SUCCESS
    }

    private fun findEngine(level: Level, seatPos: BlockPos): BlockPos? {
        // scan within 20 blocks for an engine
        for (pos in BlockPos.betweenClosed(seatPos.offset(-20, -20, -20), seatPos.offset(20, 20, 20))) {
            if (level.getBlockState(pos).block is RocketEngineBlock) return pos.immutable()
        }
        return null
    }
}
