package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.alchemy.Potion
import java.util.function.BiConsumer

@Suppress("unused")
object SpacePotions {
    private val POTIONS = linkedMapOf<ResourceLocation, Potion>()

    @JvmStatic
    fun register(r: BiConsumer<Potion, ResourceLocation>) {
        POTIONS.forEach { (k, v) -> r.accept(v, k) }
        addRecipes()
    }

    @Suppress("EmptyFunctionBlock")
    private fun addRecipes() {}

    private fun <T : Potion> make(id: String, potion: T): T {
        val old = POTIONS.put(modLoc(id), potion)
        check(old == null) { "Typo? Duplicate id $id" }
        return potion
    }
}

