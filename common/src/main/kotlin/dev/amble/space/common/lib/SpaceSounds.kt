package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import java.util.concurrent.ThreadLocalRandom
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
    val SPACE_AMBIENCE: SoundEvent = sound("space_ambience")

    @JvmStatic
    val SPACE_MUSIC_ARCADIA: SoundEvent = sound("arcadia")

    @JvmStatic
    val SPACE_MUSIC_DREAMY_FLASHBACK: SoundEvent = sound("dreamy_flashback")

    @JvmStatic
    val SPACE_MUSIC_FROZEN_STAR: SoundEvent = sound("frozen_star")

    @JvmStatic
    val SPACE_MUSIC_FROST_WALTZ: SoundEvent = sound("frost_waltz")

    @JvmStatic
    val SPACE_MUSIC_IMPACT_LENTO: SoundEvent = sound("impact_lento")

    @JvmStatic
    val SPACE_MUSIC_WIZARDTORIUM: SoundEvent = sound("wizardtorium")

    @JvmStatic
    val SPACE_MUSIC_TRACKS: List<SoundEvent> = listOf(
        SPACE_MUSIC_ARCADIA,
        SPACE_MUSIC_DREAMY_FLASHBACK,
        SPACE_MUSIC_FROZEN_STAR,
        SPACE_MUSIC_FROST_WALTZ,
        SPACE_MUSIC_IMPACT_LENTO,
        SPACE_MUSIC_WIZARDTORIUM
    )

    @JvmStatic
    fun randomSpaceMusic(): SoundEvent = SPACE_MUSIC_TRACKS[ThreadLocalRandom.current().nextInt(SPACE_MUSIC_TRACKS.size)]
}
