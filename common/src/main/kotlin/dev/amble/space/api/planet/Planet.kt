package dev.amble.space.api.planet

import dev.amble.space.api.SpaceAPI
import dev.amble.space.xplat.IXplatAbstractions
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import org.joml.Vector3f

object PlanetConstants {
    @JvmStatic
    val GRAVITY_MULTIPLIER: Float = 0.08F
}

data class Planet(
    val dimension: ResourceLocation,
    val gravity: Float = -1F,
    val oxygenRate: Float = 1F,
    val atmosphereDensity: Float = 1F,
    val temperature: Float = 237.15F,
    val atmosphereColor: Vector3f = Vector3f(
        0.4f,
        0.6f,
        1.0f
    )
) {
    fun asWorldOrNull(
        function: (ResourceKey<Level>) -> Level? = { IXplatAbstractions.INSTANCE.server()?.getLevel(it) }
    ): Level? {
        return function(ResourceKey.create(Registries.DIMENSION, dimension))
    }

    val celsius: Float get() = temperature - 273.15F
    val fahrenheit: Float get() = celsius * 1.8F + 32F
    val freezing: Boolean get() = celsius < 0F
    val modifiesGravity: Boolean get() = gravity >= 0
    val noFallDamage: Boolean get() = gameGravity < 1 && gravity > 0
    val gameGravity: Float get() = (gravity / 9.81F) * PlanetConstants.GRAVITY_MULTIPLIER
    val cubeMap: ResourceLocation get() = dimension.withPrefix("textures/environment/").withSuffix(".png")
    val clouds: Boolean get() = atmosphereDensity >= 0.3F
    val hasAtmosphereGlow: Boolean get() = atmosphereDensity > 0.1F
}

object PlanetRegistry {
    private val PLANETS = linkedMapOf<ResourceLocation, Planet>()

    fun register(planet: Planet): Planet {
        val old = PLANETS.putIfAbsent(planet.dimension, planet)
        check(old == null) { "Duplicate planet: ${planet.dimension}" }
        return planet
    }

    fun planet(
        name: String,
        builder: PlanetBuilder.() -> Unit
    ): Planet = register(
        PlanetBuilder(SpaceAPI.modLoc(name)).apply(builder).build()
    )

    fun all(): Collection<Planet> = PLANETS.values
    fun get(id: ResourceLocation): Planet? = PLANETS[id]
    fun get(level: Level): Planet? = PLANETS[level.dimension().location()]

    val EARTH = planet("earth") {
        gravity = 9.81F
        oxygenRate = 1F
        temperature = 293.15F
        atmosphereDensity = 1F
        atmosphereColor = Vector3f(0.2f, 0.5f, 1.0f)
    }

    val MARS = planet("mars") {
        gravity = 3.72F
        oxygenRate = 0F
        temperature = 210F
        atmosphereDensity = 0.06F
        atmosphereColor = Vector3f(0.8f, 0.3f, 0.1f)
    }

    val MOON = planet("moon") {
        gravity = 1.62F
        oxygenRate = 0F
        temperature = 220F
        atmosphereDensity = 0F
    }

    val SUN = planet("sun") {
        gravity = 274F
        oxygenRate = 0F
        temperature = 5778F
        atmosphereDensity = 0F
        atmosphereColor = Vector3f(1.0f, 0.8f, 0.2f)
    }
}

class PlanetBuilder(val id: ResourceLocation) {
    var gravity: Float = 9.81F
    var oxygenRate: Float = 1F
    var temperature: Float = 293.15F // 20 degs
    var atmosphereDensity: Float = 1F
    var atmosphereColor: Vector3f = Vector3f(0.4f, 0.6f, 1.0f)

    fun build() = Planet(id, gravity, oxygenRate, atmosphereDensity, temperature, atmosphereColor)
}