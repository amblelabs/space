package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI.modLoc
import dev.amble.space.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import java.util.function.BiConsumer
import java.util.function.BiFunction

object SpaceBlockEntities {
    private val BLOCK_ENTITIES = linkedMapOf<ResourceLocation, BlockEntityType<*>>()

    @JvmStatic
    fun registerTiles(r: BiConsumer<BlockEntityType<*>, ResourceLocation>) =
        BLOCK_ENTITIES.forEach { (k, v) -> r.accept(v, k) }

    private fun <T : BlockEntity> register(
        id: String,
        func: BiFunction<BlockPos, BlockState, T>,
        vararg blocks: Block
    ): BlockEntityType<T> {
        val ret = IXplatAbstractions.INSTANCE.createBlockEntityType(func, *blocks)
        val old = BLOCK_ENTITIES.put(modLoc(id), ret)
        check(old == null) { "Duplicate id $id" }
        return ret
    }
}

