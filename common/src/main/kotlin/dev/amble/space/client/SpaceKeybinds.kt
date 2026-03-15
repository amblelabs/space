package dev.amble.space.client

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import org.lwjgl.glfw.GLFW

object SpaceKeybinds {
    @JvmField
    val EXAMPLE_KEY = KeyMapping(
        "key.space.example",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "key.categories.space"
    )
}

