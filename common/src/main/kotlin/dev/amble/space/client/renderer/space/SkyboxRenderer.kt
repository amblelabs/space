package dev.amble.space.client.renderer.space

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.*
import com.mojang.math.Axis
import dev.amble.space.api.SpaceAPI
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.resources.ResourceLocation
import org.joml.Matrix4f
import org.joml.Quaternionf


object SkyboxRenderer {
    @JvmStatic
    val LOOKUP: Array<Quaternionf?> = arrayOf<Quaternionf?>(
        null, Axis.XP.rotationDegrees(90.0f),
        Axis.XP.rotationDegrees(-90.0f), Axis.XP.rotationDegrees(180.0f),
        Axis.ZP.rotationDegrees(90.0f), Axis.ZP.rotationDegrees(-90.0f), null
    )

    @JvmStatic
    val SPACE_SKY: ResourceLocation = SpaceAPI.modLoc("textures/environment/space_sky/panorama")


    private val faces: Array<ResourceLocation?> = arrayOfNulls(6)
    var texture: ResourceLocation? = null
        set(value) {
            if (field == value) return
            field = value
            for (i in faces.indices) {
                faces[i] = value?.withSuffix("_$i.png")
            }
        }

    fun draw(stack : PoseStack, tesselator: Tesselator = Tesselator.getInstance()) {
        RenderSystem.enableBlend()
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader)
        RenderSystem.depthMask(false)
        RenderSystem.disableCull()
        RenderSystem.disableDepthTest()

        for (k in 0..5) {
            stack.pushPose()
            val rot: Quaternionf? = LOOKUP[k]

            if (rot != null) {
                stack.mulPose(rot)
            }

            val matrix4f: Matrix4f? = stack.last().pose()
            val faceTexture = this.faces[k]
            if (faceTexture == null || matrix4f == null) {
                stack.popPose()
                continue
            }

            RenderSystem.setShaderTexture(0, faceTexture)

            val builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
            val l = 255
            builder.addVertex(matrix4f, -100.0f, -100.0f, -100.0f).setUv(0.0f, 0.0f).setColor(255, 255, 255, l)
            builder.addVertex(matrix4f, -100.0f, -100.0f, 100.0f).setUv(0.0f, 1.0f).setColor(255, 255, 255, l)
            builder.addVertex(matrix4f, 100.0f, -100.0f, 100.0f).setUv(1.0f, 1.0f).setColor(255, 255, 255, l)
            builder.addVertex(matrix4f, 100.0f, -100.0f, -100.0f).setUv(1.0f, 0.0f).setColor(255, 255, 255, l)
            BufferUploader.drawWithShader(builder.buildOrThrow())
            stack.popPose()
        }
        RenderSystem.depthMask(true)
        RenderSystem.enableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.disableBlend()
    }

    fun drawSpace(stack : PoseStack, tesselator: Tesselator = Tesselator.getInstance(), fade: Float = 1F) {
        val alpha = fade.coerceIn(0F, 1F)
        if (alpha <= 0F) return

        RenderSystem.setShaderColor(0.25F, 0.25F, 0.25F, alpha)
        texture = SPACE_SKY
        draw(stack, tesselator)
        RenderSystem.setShaderColor(1F, 1F, 1f, alpha)
        SolarSystemRenderer.render(stack, alpha)
        RenderSystem.setShaderColor(1F, 1F, 1f, 1F)
    }
}
