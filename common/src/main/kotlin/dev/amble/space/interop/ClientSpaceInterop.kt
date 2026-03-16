package dev.amble.space.interop

import dev.amble.space.client.SpaceKeybinds
import dev.amble.space.client.renderer.EmptyEntityRenderer
import dev.amble.space.client.sound.SpaceSoundManager
import dev.amble.space.client.sound.TrackedSpaceSounds
import dev.amble.space.common.contraption.render.RocketVisualization
import dev.amble.space.common.lib.SpaceEntities
import dev.amble.space.common.lib.SpaceSounds
import dev.amble.space.xplat.IClientXplatAbstractions

object ClientSpaceInterop {
    @JvmStatic
    fun init() {
        TrackedSpaceSounds.register()
        RocketVisualization.register()

        IClientXplatAbstractions.INSTANCE.registerEntityRenderer(SpaceEntities.ROCKET_CONTRAPTION, ::EmptyEntityRenderer)
    }

    @JvmStatic
    fun tick() {
        SpaceSoundManager.tick()
        SpaceKeybinds.tick()
    }
}