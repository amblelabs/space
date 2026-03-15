package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect
import java.util.function.BiConsumer

@Suppress("unused")
object SpaceMobEffects {
    private val EFFECTS = linkedMapOf<ResourceLocation, MobEffect>()

    @JvmStatic
    fun register(r: BiConsumer<MobEffect, ResourceLocation>) =
        EFFECTS.forEach { (k, v) -> r.accept(v, k) }

    private fun <T : MobEffect> make(id: String, effect: T): T {
        val old = EFFECTS.put(modLoc(id), effect)
        check(old == null) { "Typo? Duplicate id $id" }
        return effect
    }
}

