package dev.amble.space.fabric.client

import net.minecraft.client.color.block.BlockColor
import net.minecraft.client.color.item.ItemColor
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import java.util.function.BiConsumer

object RegisterClientStuff {

    @JvmStatic
    fun init() {
        // register client-side things here (item/block colour providers, tooltips, etc.)
    }

    @JvmStatic
    fun registerColorProviders(
        itemColorRegistry: BiConsumer<ItemColor, Item>,
        blockColorRegistry: BiConsumer<BlockColor, Block>
    ) {
        // register colour providers here
    }

    @JvmStatic
    fun registerBlockEntityRenderers(registerer: BlockEntityRendererRegisterer) {
        // register block entity renderers here
    }

    interface BlockEntityRendererRegisterer {
        fun <T : BlockEntity> registerBlockEntityRenderer(
            type: BlockEntityType<T>,
            provider: BlockEntityRendererProvider<in T>
        )
    }
}
