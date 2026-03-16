package dev.amble.space.client.sound

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource

abstract class LoopingSound(
    soundEvent: SoundEvent,
    soundSource: SoundSource
) : AbstractTickableSoundInstance(soundEvent, soundSource, RandomSource.create()) {

    init {
        looping = true
        delay = 0
        volume = 1f
        pitch = 1f
    }

    override fun tick() {}

    override fun canStartSilent() = true

    fun setPosition(pos: BlockPos) {
        x = pos.x.toDouble()
        y = pos.y.toDouble()
        z = pos.z.toDouble()
    }

    fun getPosition() = BlockPos(x.toInt(), y.toInt(), z.toInt())
}