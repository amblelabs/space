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
import net.minecraft.data.models.model.ModelLocationUtils
import net.minecraft.data.models.model.ModelTemplate
import net.minecraft.data.models.model.TextureMapping
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import java.util.Optional

class FabricSpaceModelProvider(val output: FabricDataOutput) : FabricAmbleModelProvider(output) {
    override fun generateBlockStateModels(gen: BlockModelGenerators) {
        SpaceBlocks.forEach { block, id ->
            gen.createTrivialCube(block)
        }
    }

    override fun generateItemModels(gen: ItemModelGenerators) {
        SpaceBlocks.forEach { block, id ->
            val item = block.asItem()
            if (item != Items.AIR) {
                registerItem(gen, item, output.modId)
            }
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

            if (type == Type.BLOCK || type == Type.ALL || type == Type.NONE) return

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
}

