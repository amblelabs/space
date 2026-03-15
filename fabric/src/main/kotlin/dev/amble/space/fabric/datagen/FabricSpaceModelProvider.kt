package dev.amble.space.fabric.datagen

import dev.amble.lib.datagen.AutomaticModel
import dev.amble.lib.datagen.Type
import dev.amble.lib.fabric.datagen.FabricAmbleModelProvider
import dev.amble.lib.reflection.ReflectionUtil
import dev.amble.space.api.SpaceAPI
import dev.amble.space.common.lib.SpaceBlocks
import dev.amble.space.common.lib.SpaceItems
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.data.models.ItemModelGenerators
import net.minecraft.data.models.model.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class FabricSpaceModelProvider(val output: FabricDataOutput) : FabricAmbleModelProvider(output) {
    override fun generateBlockStateModels(gen: BlockModelGenerators) {
        val blocks = ReflectionUtil.getAnnotatedValues(
            SpaceBlocks.javaClass,
            Block::class.java,
            AutomaticModel::class.java,
            false
        )
        blocks.putAll(
            ReflectionUtil.getAnnotatedValues(
                SpaceBlocks.javaClass,
                Block::class.java,
                AutomaticModel::class.java,
                true
            )
        )

        blocks.forEach { (block, optional) ->
            val type = if (optional.isEmpty) Type.BLOCK else optional.orElseThrow().type

            when {
                type == Type.ITEM || type == Type.NONE -> return@forEach
                else -> buildModel(block, gen)
            }
        }
    }

    override fun generateItemModels(gen: ItemModelGenerators) {
        val blocks = ReflectionUtil.getAnnotatedValues(
            SpaceBlocks.javaClass,
            Block::class.java,
            AutomaticModel::class.java,
            false
        )
        blocks.putAll(
            ReflectionUtil.getAnnotatedValues(
                SpaceBlocks.javaClass,
                Block::class.java,
                AutomaticModel::class.java,
                true
            )
        )

        blocks.forEach() { (block, optional) ->
            val type = if (optional.isEmpty) {
                Type.BLOCK
            } else {
                optional.orElseThrow().type
            }

            if (type == Type.BLOCK || type == Type.NONE) return

            registerItem(gen, block.asItem(), output.modId)
        }

        val items = ReflectionUtil.getAnnotatedValues(
            SpaceItems.javaClass,
            Item::class.java,
            AutomaticModel::class.java,
            false
        )
        items.putAll(
            ReflectionUtil.getAnnotatedValues(
                SpaceItems.javaClass,
                Item::class.java,
                AutomaticModel::class.java,
                true
            )
        )

        items.forEach() { (item, optional) ->
            // if none specified, its BOTH, otherwise use the specified one
            val type = if (optional.isEmpty) {
                Type.ALL
            } else {
                optional.orElseThrow().type
            }

            if (type == Type.BLOCK || type == Type.NONE) return

            registerItem(gen, item, output.modId)
        }
    }

    private fun registerItem(gen: ItemModelGenerators, item: Item, modid: String = SpaceAPI.MOD_ID) {
        item(TextureSlot.LAYER0).create(
            ModelLocationUtils.getModelLocation(item),
            createTextureMap(item, modid),
            gen.output
        )
    }

    private fun item(vararg slots: TextureSlot, modid: String = "minecraft", parent: String = "generated") : ModelTemplate {
        return ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath(modid, "item/$parent")), Optional.empty(), *slots)
    }

    private fun createTextureMap(item: Item, modid: String) : TextureMapping {
        var texture = ResourceLocation.fromNamespaceAndPath(modid, "item/${item.getItemName()}")
        if (!texture.textureExists()) texture = SpaceAPI.modLoc("item/error")
        return TextureMapping().put(TextureSlot.LAYER0, texture)
    }

    private fun Item.getItemName() : String = this.descriptionId.split("\\.").last()

    private fun ResourceLocation.textureExists() : Boolean =
        this@FabricSpaceModelProvider.output.modContainer.findPath("assets/$namespace/textures/$path.png").isPresent
    fun getSubfolders(id: ResourceLocation): List<String> = runCatching {
        val path = getResourcesPath("assets")
            .resolve(id.namespace)
            .resolve("textures")
            .resolve(id.path.substringBeforeLast("/"))

        if (!Files.exists(path)) return emptyList()

        Files.walk(path, 10).use { stream ->
            stream.filter(Files::isDirectory)
                .map { path.relativize(it).toString().replace("\\", "/") }
                .filter { it.isNotEmpty() }
                .toList()
        }
    }.getOrDefault(emptyList())

    // Extension on ResourceLocation for clean call sites
    fun ResourceLocation.findTexture(): ResourceLocation {
        // 1. exact match
        if (this.textureExists()) return this

        // 2. strip last _segment (e.g. martian_stone_side -> martian_stone)
        val stripped = path.substringBeforeLast('_')
        val strippedLoc = withPath(stripped)
        if (strippedLoc.textureExists()) return strippedLoc

        // 3. try _planks variant
        val planksLoc = withPath("${stripped}_planks")
        if (planksLoc.textureExists()) return planksLoc

        // 4. search subfolders
        val parentPath = path.substringBeforeLast("/")
        val fileName = path.substringAfterLast("/")
        getSubfolders(this).forEach { folder ->
            val attempt = withPath("$parentPath/$folder/$fileName")
            if (attempt.textureExists()) return attempt
            val recursive = attempt.findTexture()
            if (recursive != attempt && recursive.textureExists()) return recursive
        }

        // 5. recurse with further stripped path
        if (stripped.contains('_')) {
            return withPath(stripped).findTexture()
        }

        println("[ModelGenerator] Texture not found: $this")
        return this
    }

    // Convenience extensions
    fun Block.findBlockTexture(): ResourceLocation = TextureMapping.getBlockTexture(this).findTexture()
    fun Block.findTextureMapping(): TextureMapping = TextureMapping.cube(findBlockTexture())

    // replaces createSimpleBlock with MultiVariant - use ResourceLocation directly
    fun BlockModelGenerators.simpleBlock(block: Block, model: ResourceLocation) {
        blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, model))
    }

    fun fence(block: Block, gen: BlockModelGenerators) {
        val mapping = block.findTextureMapping()
        val post = ModelTemplates.FENCE_POST.create(block, mapping, gen.modelOutput)
        val side = ModelTemplates.FENCE_SIDE.create(block, mapping, gen.modelOutput)
        val inventory = ModelTemplates.FENCE_INVENTORY.create(block, mapping, gen.modelOutput)
        gen.blockStateOutput.accept(BlockModelGenerators.createFence(block, post, side))
        gen.delegateItemModel(block, inventory)
    }

    fun fenceGate(block: Block, gen: BlockModelGenerators) {
        val mapping = block.findTextureMapping()
        val open       = ModelTemplates.FENCE_GATE_OPEN.create(block, mapping, gen.modelOutput)
        val closed     = ModelTemplates.FENCE_GATE_CLOSED.create(block, mapping, gen.modelOutput)
        val wallOpen   = ModelTemplates.FENCE_GATE_WALL_OPEN.create(block, mapping, gen.modelOutput)
        val wallClosed = ModelTemplates.FENCE_GATE_WALL_CLOSED.create(block, mapping, gen.modelOutput)
        gen.blockStateOutput.accept(BlockModelGenerators.createFenceGate(block, open, closed, wallOpen, wallClosed, true))
    }

    fun trapdoor(block: Block, gen: BlockModelGenerators) {
        val mapping = TextureMapping.defaultTexture(block.findBlockTexture())
        val top    = ModelTemplates.TRAPDOOR_TOP.create(block, mapping, gen.modelOutput)
        val bottom = ModelTemplates.TRAPDOOR_BOTTOM.create(block, mapping, gen.modelOutput)
        val open   = ModelTemplates.TRAPDOOR_OPEN.create(block, mapping, gen.modelOutput)
        gen.blockStateOutput.accept(
            BlockModelGenerators.createTrapdoor(
                block,
                top,
                bottom,
                open
            )
        )
        gen.delegateItemModel(block, bottom)
    }

    fun slab(block: SlabBlock, gen: BlockModelGenerators) {
        val texture = block.findBlockTexture()
        val mapping = TextureMapping.cube(block)
            .put(TextureSlot.TOP, texture)
            .put(TextureSlot.SIDE, texture)
            .put(TextureSlot.BOTTOM, texture)
        val bottom = ModelTemplates.SLAB_BOTTOM.createWithOverride(block, "_bottom", mapping, gen.modelOutput)
        val top    = ModelTemplates.SLAB_TOP.createWithOverride(block, "_top", mapping, gen.modelOutput)
        val double = ModelTemplates.CUBE_COLUMN.createWithOverride(block, "_double", TextureMapping.column(texture, texture), gen.modelOutput)
        gen.blockStateOutput.accept(BlockModelGenerators.createSlab(block, bottom, top, double))
        gen.delegateItemModel(block, bottom)
    }

    fun stairs(block: StairBlock, gen: BlockModelGenerators) {
        val texture = block.findBlockTexture()
        val mapping = TextureMapping.cube(texture)
            .put(TextureSlot.BOTTOM, texture)
            .put(TextureSlot.TOP, texture)
            .put(TextureSlot.SIDE, texture)
        val inner    = ModelTemplates.STAIRS_INNER.createWithOverride(block, "_inner", mapping, gen.modelOutput)
        val straight = ModelTemplates.STAIRS_STRAIGHT.createWithOverride(block, "_straight", mapping, gen.modelOutput)
        val outer    = ModelTemplates.STAIRS_OUTER.createWithOverride(block, "_outer", mapping, gen.modelOutput)
        gen.blockStateOutput.accept(BlockModelGenerators.createStairs(block, inner, straight, outer))
        gen.delegateItemModel(block, straight)
    }

    fun wall(block: WallBlock, gen: BlockModelGenerators) {
        val texture = block.findBlockTexture()
        val mapping = TextureMapping.cube(block).put(TextureSlot.WALL, texture)
        val post     = ModelTemplates.WALL_POST.create(block, mapping, gen.modelOutput)
        val lowSide  = ModelTemplates.WALL_LOW_SIDE.create(block, mapping, gen.modelOutput)
        val tallSide = ModelTemplates.WALL_TALL_SIDE.create(block, mapping, gen.modelOutput)
        val inventory = ModelTemplates.WALL_INVENTORY.create(block, mapping, gen.modelOutput)
        gen.blockStateOutput.accept(BlockModelGenerators.createWall(block, post, lowSide, tallSide))
        gen.delegateItemModel(block, inventory)
    }

    private fun buildModel(block: Block, gen: BlockModelGenerators) {
        val id = block.descriptionId.substringAfterLast(".")

        when (block) {
            is FenceBlock -> fence(block, gen)
            is FenceGateBlock -> fenceGate(block, gen)
            is MultifaceBlock -> gen.createMultiface(block)
            is RotatedPillarBlock -> {
                var sideTexture = TextureMapping.getBlockTexture(block).withSuffix("_side").findTexture()
                var topTexture = TextureMapping.getBlockTexture(block).withSuffix("_top").findTexture()
                if (id.contains("_wood")) {
                    sideTexture = sideTexture.withPath(sideTexture.path.replace("_wood", "_log"))
                    topTexture = sideTexture
                }
                val model = ModelTemplates.CUBE_COLUMN.createWithOverride(
                    block, "", TextureMapping.column(sideTexture, topTexture), gen.modelOutput
                )
                gen.blockStateOutput.accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(block, model, model))
                gen.delegateItemModel(block, TexturedModel.CUBE_TOP.updateTexture { tex ->
                    tex.put(TextureSlot.SIDE, sideTexture).put(TextureSlot.TOP, topTexture)
                }.createWithSuffix(block, "_inventory", gen.modelOutput))
            }
            is TrapDoorBlock -> trapdoor(block, gen)
            is SlabBlock -> slab(block, gen)
            is StairBlock -> stairs(block, gen)
            is WallBlock -> wall(block, gen)
            is DirectionalBlock -> { /* todo */ }
            is HorizontalDirectionalBlock -> { /* todo */ }
            is GrassBlock -> {
                val mapping = TexturedModel.CUBE_TOP.updateTexture { tex ->
                    tex.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side").findTexture())
                        .put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top").findTexture())
                }
                val model = mapping.create(block, gen.modelOutput)
                gen.simpleBlock(block, model)
                gen.delegateItemModel(block, mapping.createWithSuffix(block, "_inventory", gen.modelOutput))
            }
            else -> {
                val model = ModelTemplates.CUBE_ALL.create(block, block.findTextureMapping(), gen.modelOutput)
                gen.simpleBlock(block, model)
                gen.delegateItemModel(block, TexturedModel.CUBE.updateTexture { tex ->
                    tex.put(TextureSlot.ALL, block.findBlockTexture())
                }.createWithSuffix(block, "_inventory", gen.modelOutput))
            }
        }
    }
    private val resourcesPath: Path = output.outputFolder

    fun getResourcesPath(resource: String): Path =
        resourcesPath.resolve(resource.replace("/", File.separator))
}

