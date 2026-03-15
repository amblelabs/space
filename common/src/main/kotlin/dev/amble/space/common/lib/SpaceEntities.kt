package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import java.util.function.BiConsumer

object SpaceEntities {
    private val ENTITIES = linkedMapOf<ResourceLocation, EntityType<*>>()

    @JvmStatic
    fun registerEntities(r: BiConsumer<EntityType<*>, ResourceLocation>) =
        ENTITIES.forEach { (k, v) -> r.accept(v, k) }

    @Suppress("unused")
    private fun <T : Entity> register(id: String, type: EntityType<T>): EntityType<T> {
        val old = ENTITIES.put(modLoc(id), type)
        check(old == null) { "Typo? Duplicate id $id" }
        return type
    }
}

