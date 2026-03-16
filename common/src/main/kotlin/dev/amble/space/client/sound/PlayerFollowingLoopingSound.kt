package dev.amble.space.client.sound

import net.minecraft.client.Minecraft
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource

class PlayerFollowingLoopingSound(
    soundEvent: SoundEvent,
    soundSource: SoundSource,
    volume: Float = 1f,
    pitch: Float = 1f
) : LoopingSound(soundEvent, soundSource) {

    init {
        this.volume = volume
        this.pitch = pitch
        Minecraft.getInstance().player?.blockPosition()?.let { setPosition(it) }
    }

    override fun tick() {
        val player = Minecraft.getInstance().player ?: run {
            stop()
            return
        }
        setPosition(player.blockPosition())
    }
}