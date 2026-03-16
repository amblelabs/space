package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI.modLoc
import dev.amble.space.common.entity.RocketContraptionEntity
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
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

    @JvmStatic
    val ROCKET_CONTRAPTION : EntityType<RocketContraptionEntity> = register("rocket_contraption", EntityType.Builder.of(::RocketContraptionEntity,
        MobCategory.MISC)
        .sized(1f, 1f)  // tune to your rocket size
        .clientTrackingRange(128)
        .updateInterval(1)
        .noSummon()
        .build("rocket_contraption"))
}

