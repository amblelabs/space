package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.level.block.Block
import java.util.function.BiConsumer
import kotlin.collections.component1
import kotlin.collections.component2

object SpaceSounds {
    private val SOUNDS = linkedMapOf<ResourceLocation, SoundEvent>()

    @JvmStatic
    fun registerSounds(r: BiConsumer<SoundEvent, ResourceLocation>) =
        SOUNDS.forEach { (k, v) -> r.accept(v, k) }

    private fun sound(name: String): SoundEvent {
        val id = modLoc(name)
        val sound = SoundEvent.createVariableRangeEvent(id)
        val old = SOUNDS.put(id, sound)
        check(old == null) { "Typo? Duplicate id $name" }
        return sound
    }

    @JvmStatic
    fun forEach(r: BiConsumer<ResourceLocation, SoundEvent>) =
        SOUNDS.forEach { (k, v) -> r.accept(k, v) }

    @JvmStatic
    val SPACE_AMBIENCE : SoundEvent = sound("space_ambience")
}

