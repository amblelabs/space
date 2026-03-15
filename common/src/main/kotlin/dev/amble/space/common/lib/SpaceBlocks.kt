package dev.amble.space.common.lib

import dev.amble.lib.datagen.NoBlockDrop
import dev.amble.lib.datagen.PickaxeMineable
import dev.amble.lib.datagen.Tool
import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BlockSetType
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
    fun forEach(r: BiConsumer<Block, ResourceLocation>) =
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

    private fun stair(base: Block, props: BlockBehaviour.Properties): Block =
        object : StairBlock(base.defaultBlockState(), props) {}

    private fun stoneButton(props: BlockBehaviour.Properties): Block =
        object : ButtonBlock(BlockSetType.STONE, 10, props) {}

    private fun stonePressurePlate(props: BlockBehaviour.Properties): Block =
        object : PressurePlateBlock(BlockSetType.STONE, props) {}

    // Mars
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_STONE: Block = blockItem("martian_stone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_STONE_WALL: Block = blockItem("martian_stone_wall", WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_STONE_SLAB: Block = blockItem("martian_stone_slab", SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_STONE_STAIRS: Block = blockItem("martian_stone_stairs", stair(MARTIAN_STONE, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_STONE_BUTTON: Block = blockItem("martian_stone_button", stoneButton(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BUTTON)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_STONE_PRESSURE_PLATE: Block = blockItem("martian_stone_pressure_plate", stonePressurePlate(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_PRESSURE_PLATE)))

    @JvmStatic @PickaxeMineable(tool = Tool.NONE)
    val MARTIAN_COAL_ORE: Block = blockItem("martian_coal_ore", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_COPPER_ORE: Block = blockItem("martian_copper_ore", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_ORE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_IRON_ORE: Block = blockItem("martian_iron_ore", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_GOLD_ORE: Block = blockItem("martian_gold_ore", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_ORE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_LAPIS_ORE: Block = blockItem("martian_lapis_ore", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.LAPIS_ORE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_REDSTONE_ORE: Block = blockItem("martian_redstone_ore", RedStoneOreBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_ORE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_DIAMOND_ORE: Block = blockItem("martian_diamond_ore", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_EMERALD_ORE: Block = blockItem("martian_emerald_ore", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_ORE)))

    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_COBBLESTONE: Block = blockItem("martian_cobblestone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_COBBLESTONE_WALL: Block = blockItem("martian_cobblestone_wall", WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE_WALL)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON) @NoBlockDrop
    val MARTIAN_COBBLESTONE_SLAB: Block = blockItem("martian_cobblestone_slab", SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE_SLAB)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_COBBLESTONE_STAIRS: Block = blockItem("martian_cobblestone_stairs", stair(MARTIAN_COBBLESTONE, BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE_STAIRS)))

    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MOSSY_MARTIAN_COBBLESTONE: Block = blockItem("mossy_martian_cobblestone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSSY_COBBLESTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MOSSY_MARTIAN_COBBLESTONE_WALL: Block = blockItem("mossy_martian_cobblestone_wall", WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSSY_COBBLESTONE_WALL)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MOSSY_MARTIAN_COBBLESTONE_SLAB: Block = blockItem("mossy_martian_cobblestone_slab", SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSSY_COBBLESTONE_SLAB)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MOSSY_MARTIAN_COBBLESTONE_STAIRS: Block = blockItem("mossy_martian_cobblestone_stairs", stair(MOSSY_MARTIAN_COBBLESTONE, BlockBehaviour.Properties.ofFullCopy(Blocks.MOSSY_COBBLESTONE_STAIRS)))

    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val POLISHED_MARTIAN_STONE: Block = blockItem("polished_martian_stone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_ANDESITE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val POLISHED_MARTIAN_STONE_SLAB: Block = blockItem("polished_martian_stone_slab", SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_ANDESITE_SLAB)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val POLISHED_MARTIAN_STONE_STAIRS: Block = blockItem("polished_martian_stone_stairs", stair(POLISHED_MARTIAN_STONE, BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_ANDESITE_STAIRS)))

    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val SMOOTH_MARTIAN_STONE: Block = blockItem("smooth_martian_stone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_STONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON) @NoBlockDrop
    val SMOOTH_MARTIAN_STONE_SLAB: Block = blockItem("smooth_martian_stone_slab", SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_STONE_SLAB)))

    @JvmStatic
    val MARTIAN_SAND: Block = blockItem("martian_sand", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)))

    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_SANDSTONE: Block = blockItem("martian_sandstone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_SANDSTONE_WALL: Block = blockItem("martian_sandstone_wall", WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_SANDSTONE_SLAB: Block = blockItem("martian_sandstone_slab", SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_SANDSTONE_BRICK_WALL: Block = blockItem("martian_sandstone_brick_wall", WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_SANDSTONE_BRICK_SLAB: Block = blockItem("martian_sandstone_brick_slab", SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_SANDSTONE_BRICK_STAIRS: Block = blockItem("martian_sandstone_brick_stairs", stair(POLISHED_MARTIAN_STONE, BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_SANDSTONE_STAIRS: Block = blockItem("martian_sandstone_stairs", stair(POLISHED_MARTIAN_STONE, BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val CRACKED_MARTIAN_SANDSTONE: Block = blockItem("cracked_martian_sandstone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val POLISHED_MARTIAN_SANDSTONE: Block = blockItem("polished_martian_sandstone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_SANDSTONE_PILLAR: Block = blockItem("martian_sandstone_pillar", RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_SANDSTONE_BRICKS: Block = blockItem("martian_sandstone_bricks", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val CRACKED_MARTIAN_SANDSTONE_BRICKS: Block = blockItem("cracked_martian_sandstone_bricks", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val CHISELED_MARTIAN_SANDSTONE: Block = blockItem("chiseled_martian_sandstone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))

    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_BRICKS: Block = blockItem("martian_bricks", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_BRICK_SLAB: Block = blockItem("martian_brick_slab", SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_SLAB)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_BRICK_STAIRS: Block = blockItem("martian_brick_stairs", stair(MARTIAN_BRICKS, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_STAIRS)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_BRICK_WALL: Block = blockItem("martian_brick_wall", WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_WALL)))

    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MARTIAN_PILLAR: Block = blockItem("martian_pillar", RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_PILLAR)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val CHISELED_MARTIAN_STONE: Block = blockItem("chiseled_martian_stone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CHISELED_STONE_BRICKS)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val CRACKED_MARTIAN_BRICKS: Block = blockItem("cracked_martian_bricks", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CRACKED_STONE_BRICKS)))

    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val INFESTED_MARTIAN_STONE: Block = blockItem("infested_martian_stone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.INFESTED_STONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val INFESTED_MARTIAN_COBBLESTONE: Block = blockItem("infested_martian_cobblestone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.INFESTED_COBBLESTONE)))

    // Moon
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE: Block = blockItem("anorthosite", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE_WALL: Block = blockItem("anorthosite_wall", WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE_SLAB: Block = blockItem("anorthosite_slab", SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE_STAIRS: Block = blockItem("anorthosite_stairs", stair(ANORTHOSITE, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)))

    @JvmStatic @PickaxeMineable(tool = Tool.NONE)
    val ANORTHOSITE_COAL_ORE: Block = blockItem("anorthosite_coal_ore", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE_COPPER_ORE: Block = blockItem("anorthosite_copper_ore", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_ORE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE_IRON_ORE: Block = blockItem("anorthosite_iron_ore", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE_GOLD_ORE: Block = blockItem("anorthosite_gold_ore", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_ORE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE_LAPIS_ORE: Block = blockItem("anorthosite_lapis_ore", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.LAPIS_ORE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE_REDSTONE_ORE: Block = blockItem("anorthosite_redstone_ore", RedStoneOreBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_ORE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE_DIAMOND_ORE: Block = blockItem("anorthosite_diamond_ore", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE_EMERALD_ORE: Block = blockItem("anorthosite_emerald_ore", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_ORE)))

    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val POLISHED_ANORTHOSITE: Block = blockItem("polished_anorthosite", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_ANDESITE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val POLISHED_ANORTHOSITE_SLAB: Block = blockItem("polished_anorthosite_slab", SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_ANDESITE_SLAB)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val POLISHED_ANORTHOSITE_STAIRS: Block = blockItem("polished_anorthosite_stairs", stair(POLISHED_ANORTHOSITE, BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_ANDESITE_STAIRS)))

    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val SMOOTH_ANORTHOSITE: Block = blockItem("smooth_anorthosite", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_STONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val SMOOTH_ANORTHOSITE_SLAB: Block = blockItem("smooth_anorthosite_slab", SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_STONE_SLAB)))

    @JvmStatic
    val REGOLITH: Block = blockItem("regolith", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)))

    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MOON_SANDSTONE: Block = blockItem("moon_sandstone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MOON_SANDSTONE_WALL: Block = blockItem("moon_sandstone_wall", WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MOON_SANDSTONE_SLAB: Block = blockItem("moon_sandstone_slab", SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MOON_SANDSTONE_STAIRS: Block = blockItem("moon_sandstone_stairs", stair(POLISHED_MARTIAN_STONE, BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val CRACKED_MOON_SANDSTONE: Block = blockItem("cracked_moon_sandstone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val POLISHED_MOON_SANDSTONE: Block = blockItem("polished_moon_sandstone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MOON_SANDSTONE_PILLAR: Block = blockItem("moon_sandstone_pillar", RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MOON_SANDSTONE_BRICKS: Block = blockItem("moon_sandstone_bricks", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MOON_SANDSTONE_BRICK_WALL: Block = blockItem("moon_sandstone_brick_wall", WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MOON_SANDSTONE_BRICK_SLAB: Block = blockItem("moon_sandstone_brick_slab", SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val MOON_SANDSTONE_BRICK_STAIRS: Block = blockItem("moon_sandstone_brick_stairs", stair(POLISHED_MARTIAN_STONE, BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val CRACKED_MOON_SANDSTONE_BRICKS: Block = blockItem("cracked_moon_sandstone_bricks", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val CHISELED_MOON_SANDSTONE: Block = blockItem("chiseled_moon_sandstone", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)))

    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE_BRICKS: Block = blockItem("anorthosite_bricks", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE_BRICK_SLAB: Block = blockItem("anorthosite_brick_slab", SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_SLAB)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE_BRICK_STAIRS: Block = blockItem("anorthosite_brick_stairs", stair(ANORTHOSITE_BRICKS, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_STAIRS)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE_BRICK_WALL: Block = blockItem("anorthosite_brick_wall", WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_WALL)))

    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val ANORTHOSITE_PILLAR: Block = blockItem("anorthosite_pillar", RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_PILLAR)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val CHISELED_ANORTHOSITE: Block = blockItem("chiseled_anorthosite", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CHISELED_STONE_BRICKS)))
    @JvmStatic @PickaxeMineable(tool = Tool.IRON)
    val CRACKED_ANORTHOSITE_BRICKS: Block = blockItem("cracked_anorthosite_bricks", Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CRACKED_STONE_BRICKS)))
}

