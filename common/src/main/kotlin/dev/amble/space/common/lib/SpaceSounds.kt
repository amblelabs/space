package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import java.util.function.BiConsumer

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
}

