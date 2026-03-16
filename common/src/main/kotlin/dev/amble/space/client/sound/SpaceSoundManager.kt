package dev.amble.space.client.sound

import dev.amble.space.api.planet.PlanetRegistry
import dev.amble.space.common.lib.SpaceSounds
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.AbstractSoundInstance
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.Level

data class ManagedSound(
    val factory: () -> AbstractTickableSoundInstance,
    val shouldPlay: (Minecraft) -> Boolean
)

object SpaceSoundManager {
    private val registry = mutableListOf<ManagedSound>()
    private val active = mutableMapOf<ManagedSound, AbstractTickableSoundInstance>()
    private var prevLevel : Level? = null

    fun register(sound: ManagedSound) = registry.add(sound)

    fun tick() {
        val mc = Minecraft.getInstance()
        if (mc.player == null || mc.level == null) return
        if (prevLevel != mc.level) {
            stopAll()
        }
        prevLevel = mc.level

        registry.forEach { managed ->
            val isPlaying = active.containsKey(managed)
            val shouldPlay = managed.shouldPlay(mc)

            when {
                shouldPlay && !isPlaying -> {
                    val instance = managed.factory()
                    active[managed] = instance
                    mc.soundManager.play(instance)
                }
                !shouldPlay && isPlaying -> {
                    active[managed]?.stop()
                    active.remove(managed)
                }
            }
        }
    }

    fun stopAll() {
        val mc = Minecraft.getInstance()
        active.values.forEach { it.stop() }
        active.clear()
    }
}

object TrackedSpaceSounds {
    fun register() {
        // ambience
        SpaceSoundManager.register(ManagedSound(
            factory = { PlayerFollowingLoopingSound(SpaceSounds.SPACE_AMBIENCE, SoundSource.AMBIENT, 0.4F) },
            shouldPlay = shouldPlay@{ mc ->
                val dim = mc.level ?: return@shouldPlay false
                PlanetRegistry.get(dim) != null
            }
        ))
    }
}