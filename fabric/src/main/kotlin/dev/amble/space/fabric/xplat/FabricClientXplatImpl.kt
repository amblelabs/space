package dev.amble.space.fabric.xplat

import dev.amble.space.xplat.IClientXplatAbstractions
import dev.amble.space.xplat.IXplatAbstractions
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction
import net.minecraft.client.renderer.item.ItemProperties
import net.minecraft.client.renderer.item.ItemPropertyFunction
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block

class FabricClientXplatImpl : IClientXplatAbstractions {

    override fun sendPacketToServer(packet: CustomPacketPayload) = ClientPlayNetworking.send(packet)

    override fun setRenderLayer(block: Block, type: RenderType) = BlockRenderLayerMap.INSTANCE.putBlock(block, type)

    override fun initPlatformSpecific() {
        // add optional client mod interop here
    }

    override fun <T : Entity> registerEntityRenderer(type: EntityType<out T>, renderer: EntityRendererProvider<T>) =
        EntityRendererRegistry.register(type, renderer)

    @Suppress("DEPRECATION")
    override fun registerItemProperty(item: Item, id: ResourceLocation, func: ItemPropertyFunction) =
        ItemProperties.register(item, id, UnclampedWrapper(func))

    @Suppress("DEPRECATION")
    private class UnclampedWrapper(private val inner: ItemPropertyFunction) : ClampedItemPropertyFunction {
        override fun unclampedCall(stack: ItemStack, level: ClientLevel?, entity: LivingEntity?, seed: Int) =
            inner.call(stack, level, entity, seed)
        override fun call(stack: ItemStack, level: ClientLevel?, entity: LivingEntity?, seed: Int) =
            unclampedCall(stack, level, entity, seed)
    }
}

