package dev.amble.space.common.contraption

import dev.amble.space.api.planet.Planet
import dev.amble.space.api.planet.PlanetRegistry
import dev.amble.space.common.blocks.rocket.RocketEngineBlock
import dev.amble.space.common.entity.RocketContraptionEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.entity.MoverType
import net.minecraft.world.phys.Vec3

object RocketPhysics {

    const val SURFACE_GRAVITY = 0.08f        // vanilla gravity constant per tick
    const val THRUST_PER_ENGINE = 120f       // full-throttle thrust per engine
    const val FUEL_BURN_RATE = 0.35f         // kg per tick per engine at full throttle
    const val DRAG_COEFFICIENT = 0.03f       // atmospheric drag scalar
    const val THROTTLE_STEP = 0.01f          // fixed per-tick throttle step from input
    const val MAX_TILT_SPEED = 1.25f         // max angular velocity (deg/tick)
    const val TILT_ACCEL = 0.35f             // angular acceleration from pilot input
    const val ANGULAR_DAMPING = 0.88f        // rotational damping for realistic response

    fun tick(entity: RocketContraptionEntity, level: ServerLevel) {
        val planet = PlanetRegistry.get(level)
        val gravity = planet?.gameGravity ?: SURFACE_GRAVITY
        applyControls(entity)

        val engineCount = entity.blocks.values.count { it.block is RocketEngineBlock }
        val throttle = entity.entityData.get(RocketContraptionEntity.THROTTLE)

        // --- fuel consumption ---
        val fuelConsumed = engineCount * FUEL_BURN_RATE * throttle
        entity.fuelMass = (entity.fuelMass - fuelConsumed).coerceAtLeast(0f)
        val hasFuel = entity.fuelMass > 0f

        // --- thrust vector along rocket nose ---
        val thrustMag = if (hasFuel) engineCount * THRUST_PER_ENGINE * throttle / entity.totalMass else 0f
        val yawRad   = entity.yRot * Mth.DEG_TO_RAD
        // Neutral pitch is upright. Convert to mathematical elevation angle.
        val pitchRad = (90f - entity.xRot) * Mth.DEG_TO_RAD

        val thrustX = thrustMag * (-Mth.sin(yawRad) * Mth.cos(pitchRad))
        val thrustY = thrustMag * Mth.sin(pitchRad)
        val thrustZ = thrustMag * (Mth.cos(yawRad) * Mth.cos(pitchRad))

        // --- drag --- scales with velocity squared, opposes motion
        val vel = entity.deltaMovement
        val speed = vel.length()
        val airDensity = getAirDensity(entity.y, planet)
        val dragMag = speed * speed * DRAG_COEFFICIENT * airDensity
        val drag = if (speed > 0.0001) vel.normalize().scale(-dragMag.toDouble()) else Vec3.ZERO

        // --- integrate ---
        entity.deltaMovement = vel
            .add(thrustX.toDouble(), thrustY.toDouble(), thrustZ.toDouble())
            .add(drag)
            .add(0.0, -gravity.toDouble(), 0.0)

        entity.deltaMovement = entity.resolveMovementAgainstWorld(entity.deltaMovement)

        entity.move(MoverType.SELF, entity.deltaMovement)
    }

    private fun applyControls(entity: RocketContraptionEntity) {
        val throttleDelta = when {
            entity.throttleUp && !entity.throttleDown -> THROTTLE_STEP
            entity.throttleDown && !entity.throttleUp -> -THROTTLE_STEP
            else -> 0f
        }
        if (throttleDelta != 0f) {
            val nextThrottle = (entity.entityData.get(RocketContraptionEntity.THROTTLE) + throttleDelta)
                .coerceIn(0f, 1f)
            entity.entityData.set(RocketContraptionEntity.THROTTLE, nextThrottle)
        }

        val pitchInput = entity.pitchInput.coerceIn(-1f, 1f)
        val yawInput = entity.yawInput.coerceIn(-1f, 1f)

        entity.pitchRate = (entity.pitchRate + pitchInput * TILT_ACCEL)
            .coerceIn(-MAX_TILT_SPEED, MAX_TILT_SPEED) * ANGULAR_DAMPING
        entity.yawRate = (entity.yawRate + yawInput * TILT_ACCEL)
            .coerceIn(-MAX_TILT_SPEED, MAX_TILT_SPEED) * ANGULAR_DAMPING

        entity.xRotO = entity.xRot
        entity.yRotO = entity.yRot

        entity.xRot += entity.pitchRate
        entity.yRot += entity.yawRate
        entity.xRot = Mth.wrapDegrees(entity.xRot)
        entity.yRot = Mth.wrapDegrees(entity.yRot)
    }

    private fun getAirDensity(altitude: Double, planet: Planet?): Float {
        if (planet == null) return 1f
        val atmosphereTop = 500.0  // tune per planet/dimension
        return (planet.atmosphereDensity * (1.0 - (altitude / atmosphereTop)))
            .toFloat()
            .coerceIn(0f, 1f)
    }
}