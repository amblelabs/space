package dev.amble.space.client

import com.mojang.blaze3d.platform.InputConstants
import dev.amble.space.common.entity.RocketContraptionEntity
import dev.amble.space.network.c2s.ForceDismountPacket
import dev.amble.space.network.c2s.RocketControlPacket
import dev.amble.space.network.c2s.ThrottlePacket
import dev.amble.space.xplat.IClientXplatAbstractions
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW

object SpaceKeybinds {

    private val KEYBINDS = mutableListOf<KeyMapping>()

    private fun register(key: KeyMapping): KeyMapping {
        KEYBINDS.add(key)
        return key
    }

    @JvmField
    val THROTTLE_MAX = register(KeyMapping(
        "key.space.throttle_max",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_Z,
        "key.categories.space"
    ))

    @JvmField
    val THROTTLE_ZERO = register(KeyMapping(
        "key.space.throttle_zero",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_X,
        "key.categories.space"
    ))

    @JvmField
    val FORCE_DISMOUNT = register(KeyMapping(
        "key.space.force_dismount",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        "key.categories.space"
    ))

    fun all(): List<KeyMapping> = KEYBINDS
    @JvmStatic
    fun tick() {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        if (player.vehicle !is RocketContraptionEntity) return
        val window = mc.window.window

        val shiftDown =
            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) ||
            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT)
        val controlDown =
            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) ||
            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL)

        // World-relative command inputs: W/S control pitch, A/D control yaw.
        val pitchInput = when {
            mc.options.keyUp.isDown && !mc.options.keyDown.isDown -> -1f
            mc.options.keyDown.isDown && !mc.options.keyUp.isDown -> 1f
            else -> 0f
        }
        val yawInput = when {
            mc.options.keyLeft.isDown && !mc.options.keyRight.isDown -> -1f
            mc.options.keyRight.isDown && !mc.options.keyLeft.isDown -> 1f
            else -> 0f
        }

        IClientXplatAbstractions.INSTANCE.sendPacketToServer(
            RocketControlPacket(
                pitchInput = pitchInput,
                yawInput = yawInput,
                throttleUp = shiftDown,
                throttleDown = controlDown
            )
        )

        when {
            THROTTLE_MAX.consumeClick() ->
                IClientXplatAbstractions.INSTANCE.sendPacketToServer(ThrottlePacket(1.0f))
            THROTTLE_ZERO.consumeClick() ->
                IClientXplatAbstractions.INSTANCE.sendPacketToServer(ThrottlePacket(0.0f))
        }

        if (FORCE_DISMOUNT.consumeClick()) {
            IClientXplatAbstractions.INSTANCE.sendPacketToServer(ForceDismountPacket)
        }
    }
}

