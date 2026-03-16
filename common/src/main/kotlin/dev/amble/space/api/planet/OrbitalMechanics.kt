package dev.amble.space.api.planet

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import java.lang.Math.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object OrbitalMechanics {

    private const val G = 6.674e-11

    fun period(semiMajorAxisKm: Double, parentMassKg: Double): Double {
        val a = semiMajorAxisKm * 1000.0
        return 2 * PI * sqrt((a * a * a) / (G * parentMassKg))
    }

    fun currentAngle(element: OrbitalElement, system: SolarSystem, elapsedSeconds: Double): Double {
        val parentMass = resolveParentMass(element, system)
        val T = period(element.semiMajorAxis, parentMass)
        val meanAnomaly = (2 * PI * (elapsedSeconds / T)) + element.epochAngle
        return solveKepler(meanAnomaly, element.eccentricity)
    }

    fun position(element: OrbitalElement, system: SolarSystem, elapsedSeconds: Double): Vec3 {
        val angle = currentAngle(element, system, elapsedSeconds)
        val e = element.eccentricity
        val r = element.semiMajorAxis * (1 - e * e) / (1 + e * cos(angle))
        val incRad = toRadians(element.inclination.toDouble())
        return Vec3(
            r * cos(angle),
            r * sin(angle) * sin(incRad),
            r * sin(angle) * cos(incRad)
        )
    }

    // Absolute position by walking up the parent chain
    fun absolutePosition(id: ResourceLocation, system: SolarSystem, elapsedSeconds: Double): Vec3 {
        val element = system.findElement(id) ?: return Vec3.ZERO
        val local = position(element, system, elapsedSeconds)
        val parentId = element.parentId ?: return local     // parent is the star, star is at origin
        val parentPos = absolutePosition(parentId, system, elapsedSeconds)
        return parentPos.add(local)
    }

    private fun resolveParentMass(element: OrbitalElement, system: SolarSystem): Double {
        val parentId = element.parentId ?: return system.starMass
        return system.findElement(parentId)?.body?.mass ?: system.starMass
    }

    private fun solveKepler(meanAnomaly: Double, e: Double): Double {
        var E = meanAnomaly
        repeat(10) { E = meanAnomaly + e * sin(E) }
        return 2 * atan2(
            sqrt(1 + e) * sin(E / 2),
            sqrt(1 - e) * cos(E / 2)
        )
    }
}