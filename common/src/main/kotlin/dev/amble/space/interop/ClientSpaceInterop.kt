package dev.amble.space.interop

import dev.amble.space.client.sound.SpaceSoundManager
import dev.amble.space.client.sound.TrackedSpaceSounds
import dev.amble.space.common.lib.SpaceSounds

object ClientSpaceInterop {
    @JvmStatic
    fun init() {
        TrackedSpaceSounds.register()
    }

    @JvmStatic
    fun tick() {
        SpaceSoundManager.tick()
    }
}