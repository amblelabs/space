package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType
import java.util.function.BiConsumer

@Suppress("unused")
object SpaceLootFunctions {
    private val LOOT_FUNCS = linkedMapOf<ResourceLocation, LootItemFunctionType<*>>()

    @JvmStatic
    fun registerSerializers(r: BiConsumer<LootItemFunctionType<*>, ResourceLocation>) =
        LOOT_FUNCS.forEach { (k, v) -> r.accept(v, k) }

    private fun register(id: String, lift: LootItemFunctionType<*>): LootItemFunctionType<*> {
        val old = LOOT_FUNCS.put(modLoc(id), lift)
        check(old == null) { "Duplicate id $id" }
        return lift
    }
}

