package dev.amble.space.client.renderer.space

import com.mojang.blaze3d.vertex.PoseStack
import dev.amble.space.api.planet.OrbitalMechanics
import dev.amble.space.api.planet.Planet
import dev.amble.space.api.planet.PlanetRegistry
import dev.amble.space.api.planet.SolarSystemRegistry
import dev.amble.space.api.planet.client.ClientSolarSystem
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3

object SolarSystemRenderer {

    private const val RENDER_DISTANCE = 360.0
    private const val BASE_BODY_HALF_SIZE = 100.0
    private const val VISUAL_SCALE_MULTIPLIER = 2.0
    private const val MIN_SCALE = 0.004f
    private const val MAX_SCALE = 6.5f
    private const val STAR_MIN_SCALE = 0.016f
    private const val CURRENT_PLANET_MIN_ALTITUDE = 255.0
    private const val CURRENT_PLANET_START_SCALE = 1.35f
    private const val CURRENT_PLANET_MIN_SCALE = 0.42f
    private const val CURRENT_PLANET_SHRINK_PER_BLOCK = 0.0019f
    private const val CURRENT_PLANET_DISTANCE_GROWTH = 0.85

    fun render(stack: PoseStack, alpha: Float = 1f) {
        val planet = PlanetRegistry.get(Minecraft.getInstance().level ?: return) ?: return
        render(stack, planet, alpha)
    }

    fun render(stack: PoseStack, currentPlanet: Planet, alpha: Float = 1f) {
        val system = SolarSystemRegistry.systemContaining(currentPlanet) ?: return
        val elapsed = ClientSolarSystem.elapsedSeconds
        val currentPos = OrbitalMechanics.absolutePosition(currentPlanet.dimension, system, elapsed)
        val renderables = mutableListOf<RenderableBody>()

        renderCurrentPlanetBelow(stack, currentPlanet, alpha)

        for (element in system.allElements()) {
            if (element.body.id == currentPlanet.dimension) continue

            val planet = element.body.planet ?: continue   // skip if no Planet registered

            val bodyPos = OrbitalMechanics.absolutePosition(element.body.id, system, elapsed)
            val relative = bodyPos.subtract(currentPos)
            val distance = relative.length()
            if (distance <= 1.0e-6) continue

            renderables += RenderableBody(
                planet = planet,
                direction = relative.normalize(),
                distance = distance,
                scale = apparentScale(element.body.radius, distance)
            )
        }

        val starPlanet = PlanetRegistry.get(system.starId)
        if (starPlanet != null) {
            val starPos = Vec3.ZERO // star is always at system origin
            val relative = starPos.subtract(currentPos)
            val distance = relative.length()
            if (distance > 1.0e-6) {
                renderables += RenderableBody(
                    planet = starPlanet,
                    direction = relative.normalize(),
                    distance = distance,
                    scale = apparentScale(system.starRadius, distance).coerceAtLeast(STAR_MIN_SCALE)
                )
            }
        }

        // Draw far-to-near so closer bodies naturally cover distant ones without depth writes.
        renderables
            .sortedByDescending { it.distance }
            .forEach { body ->
                val normalized = body.direction.scale(RENDER_DISTANCE)
                stack.pushPose()
                stack.translate(normalized.x, normalized.y, normalized.z)
                stack.scale(body.scale, body.scale, body.scale)
                PlanetRenderer.planet = body.planet
                PlanetRenderer.draw(stack, alphaMultiplier = alpha)
                stack.popPose()
            }
    }

    private fun apparentScale(radiusKm: Double, distanceKm: Double): Float {
        // Preserve physical angular ratios and only boost visibility by a small global multiplier.
        val angularScale = (radiusKm / distanceKm) * (RENDER_DISTANCE / BASE_BODY_HALF_SIZE)
        return (angularScale * VISUAL_SCALE_MULTIPLIER).toFloat().coerceIn(MIN_SCALE, MAX_SCALE)
    }

    private fun renderCurrentPlanetBelow(stack: PoseStack, currentPlanet: Planet, alpha: Float) {
        val cameraY = Minecraft.getInstance().cameraEntity?.y ?: return
        if (cameraY <= CURRENT_PLANET_MIN_ALTITUDE) return

        val altitudeOverThreshold = (cameraY - CURRENT_PLANET_MIN_ALTITUDE).coerceAtLeast(0.0)
        // Start visible at Y=255, then shrink and move farther away as altitude increases.
        val underScale = (CURRENT_PLANET_START_SCALE - altitudeOverThreshold.toFloat() * CURRENT_PLANET_SHRINK_PER_BLOCK)
            .coerceAtLeast(CURRENT_PLANET_MIN_SCALE)
        val underDistance = RENDER_DISTANCE + altitudeOverThreshold * CURRENT_PLANET_DISTANCE_GROWTH

        stack.pushPose()
        stack.translate(0.0, -underDistance, 0.0)
        stack.scale(underScale, underScale, underScale)
        PlanetRenderer.planet = currentPlanet
        PlanetRenderer.draw(stack, alphaMultiplier = alpha)
        stack.popPose()
    }

    private data class RenderableBody(
        val planet: Planet,
        val direction: Vec3,
        val distance: Double,
        val scale: Float
    )
}