package dev.amble.space.common.contraption

import dev.amble.space.common.blocks.rocket.RocketComponentBlock
import dev.amble.space.common.blocks.rocket.RocketEngineBlock
import dev.amble.space.common.blocks.rocket.RocketFuelTankBlock
import dev.amble.space.common.blocks.rocket.RocketSeatBlock
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

data class RocketStructure(
    val blocks: Map<BlockPos, BlockState>,
    val rootPos: BlockPos,
    val totalMass: Float,
    val thrustBlocks: List<BlockPos>
) {
    companion object {
        fun validate(level: Level, rootPos: BlockPos): RocketStructure? {
            val blocks = mutableMapOf<BlockPos, BlockState>()
            val toScan = ArrayDeque<BlockPos>()
            val visited = mutableSetOf<BlockPos>()
            toScan.add(rootPos)

            while (toScan.isNotEmpty()) {
                val pos = toScan.removeFirst()
                if (!visited.add(pos)) continue
                val state = level.getBlockState(pos)
                if (!isRocketBlock(state)) continue
                blocks[pos] = state
                if (blocks.size > 512) return null  // too large

                Direction.entries.forEach { dir ->
                    val neighbor = pos.relative(dir)
                    if (neighbor !in visited) toScan.add(neighbor)
                }
            }

            return if (isValidRocket(blocks)) {
                RocketStructure(
                    blocks = blocks,
                    rootPos = rootPos,
                    totalMass = calculateMass(blocks),
                    thrustBlocks = blocks.filter { isEngine(it.value) }.map { it.key }
                )
            } else null
        }

        private fun isRocketBlock(state: BlockState): Boolean =
            state.block is RocketComponentBlock

        private fun isEngine(state: BlockState): Boolean =
            state.block is RocketEngineBlock

        private fun isSeat(state: BlockState): Boolean =
            state.block is RocketSeatBlock

        private fun isFuelTank(state: BlockState): Boolean =
            state.block is RocketFuelTankBlock

        private fun isValidRocket(blocks: Map<BlockPos, BlockState>): Boolean {
            if (blocks.isEmpty()) return false
            val hasEngine = blocks.values.any { isEngine(it) }
            val hasSeat = blocks.values.any { isSeat(it) }
            val hasFuel = blocks.values.any { isFuelTank(it) }
            return hasEngine && hasSeat && hasFuel
        }

        private fun calculateMass(blocks: Map<BlockPos, BlockState>): Float =
            blocks.values.sumOf { state ->
                when (state.block) {
                    is RocketEngineBlock -> 200.0
                    is RocketFuelTankBlock -> 100.0  // dry mass only
                    is RocketSeatBlock -> 50.0
                    else -> 30.0
                }
            }.toFloat()
    }
}