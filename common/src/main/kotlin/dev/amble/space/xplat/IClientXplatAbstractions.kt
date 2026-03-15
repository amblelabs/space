package dev.amble.space.xplat

import dev.amble.space.api.SpaceAPI
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.item.ItemPropertyFunction
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import java.util.ServiceLoader

@Suppress("unused")
interface IClientXplatAbstractions {
    fun sendPacketToServer(packet: CustomPacketPayload)
    fun setRenderLayer(block: Block, type: RenderType)
    fun initPlatformSpecific()
    fun <T : Entity> registerEntityRenderer(type: EntityType<out T>, renderer: EntityRendererProvider<T>)

    @Suppress("Deprecation")
    fun registerItemProperty(item: Item, id: ResourceLocation, func: ItemPropertyFunction)

    companion object {
        val INSTANCE: IClientXplatAbstractions = run {
            val providers = ServiceLoader.load(IClientXplatAbstractions::class.java).stream().toList()
            check(providers.size == 1) {
                val names = providers.joinToString(",", "[", "]") { it.type().name }
                "There should be exactly one IClientXplatAbstractions implementation on the classpath. Found: $names"
            }
            val provider = providers.first()
            SpaceAPI.LOGGER.debug("Instantiating client xplat impl: {}", provider.type().name)
            provider.get()
        }
    }
}
