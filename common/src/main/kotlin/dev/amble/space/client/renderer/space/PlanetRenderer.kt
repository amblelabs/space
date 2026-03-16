package dev.amble.space.client.renderer.space

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.*
import com.mojang.math.Axis
import dev.amble.space.api.planet.Planet
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.resources.ResourceLocation
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.lwjgl.opengl.GL11
import kotlin.math.ln

object PlanetRenderer {

    // Standard cubemap cross layout (4x3): top, bottom, left, front, right, back.
    private val FACE_UVS = arrayOf(
        floatArrayOf(1/4f, 0/4f, 2/4f, 1/4f),
        floatArrayOf(2/4f, 0/4f, 3/4f, 1/4f),
        floatArrayOf(0/4f, 1/4f, 1/4f, 2/4f),
        floatArrayOf(1/4f, 1/4f, 2/4f, 2/4f),
        floatArrayOf(2/4f, 1/4f, 3/4f, 2/4f),
        floatArrayOf(3/4f, 1/4f, 4/4f, 2/4f),
    )
    private val LOOKUP: Array<Quaternionf?> = arrayOf(
        null,
        Axis.XP.rotationDegrees(90.0f),
        Axis.XP.rotationDegrees(-90.0f),
        Axis.XP.rotationDegrees(180.0f),
        Axis.ZP.rotationDegrees(90.0f),
        Axis.ZP.rotationDegrees(-90.0f)
    )

    private var currentPlanet: Planet? = null
    private val ATMOSPHERE_TEXTURE = ResourceLocation.fromNamespaceAndPath("space", "textures/environment/atmosphere.png")

    var planet: Planet? = null
        set(value) {
            if (field == value) return
            field = value
            currentPlanet = value
        }

    fun draw(stack: PoseStack, tesselator: Tesselator = Tesselator.getInstance(), alphaMultiplier: Float = 1f) {
        val planet = currentPlanet ?: return
        val tex = planet.cubeMap
        val visibility = alphaMultiplier.coerceIn(0f, 1f)
        if (visibility <= 0f) return

        // Solid body pass: cull back faces to avoid face-overlap artifacts near the view center.
        RenderSystem.disableBlend()
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader)
        RenderSystem.setShaderTexture(0, tex)
        RenderSystem.depthMask(false)
        RenderSystem.enableCull()
        RenderSystem.disableDepthTest()
        RenderSystem.depthFunc(GL11.GL_ALWAYS)
        drawCubeFaces(stack, tesselator, tex, (255f * visibility).toInt().coerceIn(0, 255))

        if (planet.hasAtmosphereGlow) {
            RenderSystem.enableBlend()
            RenderSystem.disableCull()
            drawAtmosphere(stack, tesselator, planet, visibility)
        }

        RenderSystem.depthFunc(GL11.GL_LEQUAL)  // restore default
        RenderSystem.depthMask(true)
        RenderSystem.enableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.disableBlend()
    }

    private fun drawCubeFaces(
        stack: PoseStack,
        tesselator: Tesselator,
        texture: ResourceLocation,
        alpha: Int,
        r: Int = 255,
        g: Int = 255,
        b: Int = 255
    ) {
        RenderSystem.setShaderTexture(0, texture)

        for (k in 0..5) {
            stack.pushPose()
            LOOKUP[k]?.let { stack.mulPose(it) }

            val matrix4f: Matrix4f = stack.last().pose()
            val uv = FACE_UVS[k]
            val uMin = uv[0]; val vMin = uv[1]
            val uMax = uv[2]; val vMax = uv[3]

            val builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
            builder.addVertex(matrix4f, -100.0f, -100.0f, -100.0f).setUv(uMin, vMin).setColor(r, g, b, alpha)
            builder.addVertex(matrix4f, -100.0f, -100.0f,  100.0f).setUv(uMin, vMax).setColor(r, g, b, alpha)
            builder.addVertex(matrix4f,  100.0f, -100.0f,  100.0f).setUv(uMax, vMax).setColor(r, g, b, alpha)
            builder.addVertex(matrix4f,  100.0f, -100.0f, -100.0f).setUv(uMax, vMin).setColor(r, g, b, alpha)
            BufferUploader.drawWithShader(builder.buildOrThrow())

            stack.popPose()
        }
    }

    private fun drawAtmosphere(stack: PoseStack, tesselator: Tesselator, planet: Planet, alphaMultiplier: Float) {
        val color = planet.atmosphereColor
        val density = planet.atmosphereDensity.coerceIn(0f, 1f)
        val shellCount = 5

        for (i in 0 until shellCount) {
            stack.pushPose()

            val scale = 1.0f + ((if (i != 0) i else i + 1) * 0.025f)
            stack.scale(scale, scale, scale)

            val alpha = ((0.11f - ln((i + 1).toDouble()) * 0.009f) * density * alphaMultiplier * 255)
                .toInt().coerceIn(0, 255)

            val tint = 0.015f * i
            val r = ((color.x + tint).coerceIn(0f, 1f) * 255).toInt()
            val g = ((color.y + tint).coerceIn(0f, 1f) * 255).toInt()
            val b = ((color.z + tint).coerceIn(0f, 1f) * 255).toInt()

            // atmosphere uses its own flat texture, not the cubemap — full UVs
            drawFlatFaces(stack, tesselator, ATMOSPHERE_TEXTURE, alpha, r, g, b)

            if (i == 1 && planet.clouds && density >= 0.55f) {
                drawCloudLayer(stack, tesselator, alphaMultiplier)
            }

            stack.popPose()
        }
    }

    // For atmosphere/clouds we use a plain texture stretched across each face (no cubemap UVs)
    private fun drawFlatFaces(
        stack: PoseStack,
        tesselator: Tesselator,
        texture: ResourceLocation,
        alpha: Int,
        r: Int = 255,
        g: Int = 255,
        b: Int = 255
    ) {
        RenderSystem.setShaderTexture(0, texture)

        for (k in 0..5) {
            stack.pushPose()
            LOOKUP[k]?.let { stack.mulPose(it) }

            val matrix4f: Matrix4f = stack.last().pose()
            val builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
            builder.addVertex(matrix4f, -100.0f, -100.0f, -100.0f).setUv(0f, 0f).setColor(r, g, b, alpha)
            builder.addVertex(matrix4f, -100.0f, -100.0f,  100.0f).setUv(0f, 1f).setColor(r, g, b, alpha)
            builder.addVertex(matrix4f,  100.0f, -100.0f,  100.0f).setUv(1f, 1f).setColor(r, g, b, alpha)
            builder.addVertex(matrix4f,  100.0f, -100.0f, -100.0f).setUv(1f, 0f).setColor(r, g, b, alpha)
            BufferUploader.drawWithShader(builder.buildOrThrow())

            stack.popPose()
        }
    }

    private fun drawCloudLayer(stack: PoseStack, tesselator: Tesselator, alphaMultiplier: Float) {
        val cloudTexture = ResourceLocation.withDefaultNamespace("textures/environment/clouds.png")
        val visibility = alphaMultiplier.coerceIn(0f, 1f)
        // Larger, sparser cloud shells with reduced opacity.
        drawFlatFaces(stack, tesselator, cloudTexture, (30f * visibility).toInt().coerceIn(0, 255), 240, 240, 240)
        stack.pushPose()
        stack.mulPose(Axis.YP.rotationDegrees(35.0f))
        stack.scale(1.10f, 1.10f, 1.10f)
        drawFlatFaces(stack, tesselator, cloudTexture, (16f * visibility).toInt().coerceIn(0, 255), 255, 255, 255)
        stack.popPose()
    }

    fun Planet.draw(stack: PoseStack, tesselator: Tesselator = Tesselator.getInstance()) {
        planet = this
        draw(stack, tesselator)
    }
}