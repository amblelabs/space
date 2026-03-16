package dev.amble.space.common.blockentity.rocket

import dev.amble.space.common.blocks.rocket.RocketFuelTankBlock
import dev.amble.space.common.lib.SpaceBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class RocketFuelTankBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(SpaceBlockEntities.ROCKET_FUEL_TANK, pos, state) {

    var fuel: Float = RocketFuelTankBlock.MAX_FUEL

    override fun saveAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(nbt, registries)
        nbt.putFloat("Fuel", fuel)
    }

    override fun loadAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(nbt, registries)
        fuel = nbt.getFloat("Fuel")
    }

    fun drainFuel(amount: Float): Float {
        val drained = minOf(fuel, amount)
        fuel -= drained
        setChanged()
        return drained
    }

    fun isEmpty() = fuel <= 0f
    fun isFull() = fuel >= RocketFuelTankBlock.MAX_FUEL
}