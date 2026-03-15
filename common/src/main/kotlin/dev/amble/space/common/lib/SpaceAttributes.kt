package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.attributes.Attribute
import java.util.function.BiConsumer

object SpaceAttributes {
    private val ATTRIBUTES = linkedMapOf<ResourceLocation, Attribute>()

    @JvmStatic
    fun register(r: BiConsumer<Attribute, ResourceLocation>) =
        ATTRIBUTES.forEach { (k, v) -> r.accept(v, k) }

    @Suppress("unused")
    private fun <T : Attribute> make(id: String, attr: T): T {
        val old = ATTRIBUTES.put(modLoc(id), attr)
        check(old == null) { "Typo? Duplicate id $id" }
        return attr
    }
}

