package dev.amble.space.api.mod

import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.stats.StatFormatter
import net.minecraft.stats.Stats

@Suppress("unused")
object SpaceStatistics {

    @JvmStatic
    fun register() {
        // register custom statistics here
    }

    private fun makeCustomStat(key: String, formatter: StatFormatter) =
        modLoc(key).also { rl ->
            Registry.register(BuiltInRegistries.CUSTOM_STAT, key, rl)
            Stats.CUSTOM.get(rl, formatter)
        }
}

