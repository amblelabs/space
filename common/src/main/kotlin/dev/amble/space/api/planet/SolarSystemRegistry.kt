package dev.amble.space.api.planet

import dev.amble.space.api.SpaceAPI
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

object SolarSystemRegistry {
    private val SYSTEMS = linkedMapOf<ResourceLocation, SolarSystem>()

    fun register(system: SolarSystem): SolarSystem {
        val old = SYSTEMS.putIfAbsent(system.id, system)
        check(old == null) { "Duplicate solar system: ${system.id}" }
        return system
    }

    fun all(): Collection<SolarSystem> = SYSTEMS.values
    fun get(id: ResourceLocation): SolarSystem? = SYSTEMS[id]

    fun systemContaining(planet: Planet): SolarSystem? =
        SYSTEMS.values.find { system ->
            system.allElements().any { it.body.id == planet.dimension }
        }

    fun systemContaining(level: Level): SolarSystem? =
        PlanetRegistry.get(level)?.let { systemContaining(it) }

    val SOLAR_SYSTEM = register(
        SolarSystem(
            id = SpaceAPI.modLoc("solar_system"),
            starId = SpaceAPI.modLoc("sun"),
            starMass = 1.989e30,
            starRadius = 696_000.0,
            elements = listOf(

                OrbitalElement(
                    body = OrbitalBody(
                        id = SpaceAPI.modLoc("earth"),
                        mass = 5.972e24,
                        radius = 6371.0
                    ),
                    parentId = null,                // orbits the star
                    semiMajorAxis = 149_600_000.0,
                    eccentricity = 0.0167,
                    inclination = 0.0f,
                    epochAngle = 0.0f,
                    children = listOf(

                        OrbitalElement(
                            body = OrbitalBody(
                                id = SpaceAPI.modLoc("moon"),
                                mass = 7.342e22,
                                radius = 1737.0
                            ),
                            parentId = SpaceAPI.modLoc("earth"),
                            semiMajorAxis = 384_400.0,
                            eccentricity = 0.0549,
                            inclination = 5.1f,
                            epochAngle = 0.0f
                        )
                    )
                ),

                OrbitalElement(
                    body = OrbitalBody(
                        id = SpaceAPI.modLoc("mars"),
                        mass = 6.390e23,
                        radius = 3389.5
                    ),
                    parentId = null,                // orbits the star
                    semiMajorAxis = 227_900_000.0,
                    eccentricity = 0.0934,
                    inclination = 1.85f,
                    epochAngle = 0.523f,            // Mars starts ~30 degrees ahead of Earth
                    children = emptyList()
                )
            )
        )
    )
}