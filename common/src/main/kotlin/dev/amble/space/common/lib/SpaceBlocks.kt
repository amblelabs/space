package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction
import java.util.function.BiConsumer
import java.util.function.Consumer

@Suppress("unused", "MemberVisibilityCanBePrivate")
object SpaceBlocks {
    private val BLOCKS = linkedMapOf<ResourceLocation, Block>()
    private val BLOCK_ITEMS = linkedMapOf<ResourceLocation, Pair<Block, Item.Properties>>()
    private val BLOCK_TABS = linkedMapOf<CreativeModeTab, MutableList<Block>>()

    @JvmStatic
    fun registerBlocks(r: BiConsumer<Block, ResourceLocation>) =
        BLOCKS.forEach { (k, v) -> r.accept(v, k) }

    @JvmStatic
    fun registerBlockItems(r: BiConsumer<Item, ResourceLocation>) =
        BLOCK_ITEMS.forEach { (k, v) -> r.accept(net.minecraft.world.item.BlockItem(v.first, v.second), k) }

    @JvmStatic
    fun registerBlockCreativeTab(r: Consumer<Block>, tab: CreativeModeTab) =
        BLOCK_TABS.getOrDefault(tab, emptyList()).forEach(r::accept)

    // --- block property helpers ---

    fun papery(color: MapColor): BlockBehaviour.Properties = BlockBehaviour.Properties.of()
        .mapColor(color).sound(SoundType.GRASS).instabreak().ignitedByLava()
        .pushReaction(PushReaction.DESTROY)

    fun woodyHard(color: MapColor): BlockBehaviour.Properties = BlockBehaviour.Properties
        .ofFullCopy(Blocks.OAK_LOG).mapColor(color).sound(SoundType.WOOD).strength(3f, 4f)

    fun woody(color: MapColor): BlockBehaviour.Properties = BlockBehaviour.Properties
        .ofFullCopy(Blocks.OAK_LOG).mapColor(color).sound(SoundType.WOOD).strength(2f)

    // --- registration helpers ---

    fun <T : Block> blockItem(name: String, block: T, tab: CreativeModeTab? = SpaceCreativeTabs.SPACE): T =
        blockItem(name, block, SpaceItems.props(), tab)

    fun <T : Block> blockItem(name: String, block: T, props: Item.Properties, tab: CreativeModeTab? = SpaceCreativeTabs.SPACE): T {
        blockNoItem(name, block)
        val old = BLOCK_ITEMS.put(modLoc(name), Pair(block, props))
        check(old == null) { "Duplicate id $name" }
        if (tab != null) BLOCK_TABS.getOrPut(tab) { mutableListOf() }.add(block)
        return block
    }

    fun <T : Block> blockNoItem(name: String, block: T): T {
        val old = BLOCKS.put(modLoc(name), block)
        check(old == null) { "Typo? Duplicate id $name" }
        return block
    }
}

