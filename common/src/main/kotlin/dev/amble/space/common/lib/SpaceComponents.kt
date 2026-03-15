package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.core.component.DataComponentType
import net.minecraft.resources.ResourceLocation
import java.util.function.BiConsumer
import java.util.function.UnaryOperator

object SpaceComponents {
    private val COMPONENTS = linkedMapOf<ResourceLocation, DataComponentType<*>>()

    @JvmStatic
    fun registerComponents(r: BiConsumer<DataComponentType<*>, ResourceLocation>) =
        COMPONENTS.forEach { (k, v) -> r.accept(v, k) }

    private fun <T> make(name: String, op: UnaryOperator<DataComponentType.Builder<T>>): DataComponentType<T> {
        val type = op.apply(DataComponentType.builder()).build()
        COMPONENTS[modLoc(name)] = type
        return type
    }
}

