package dev.amble.lib.datagen

import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class PickaxeMineable(
    val tool: Tool = Tool.NONE)

enum class Tool(@JvmField val tag: TagKey<Block>?) {
    NONE(null),
    STONE(BlockTags.NEEDS_STONE_TOOL),
    IRON(BlockTags.NEEDS_IRON_TOOL),
    DIAMOND(BlockTags.NEEDS_DIAMOND_TOOL),
}