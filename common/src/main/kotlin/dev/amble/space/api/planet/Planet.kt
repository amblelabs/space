package dev.amble.space.api.planet

import dev.amble.space.api.SpaceAPI
import dev.amble.space.xplat.IXplatAbstractions
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor

object PlanetConstants {
    @JvmStatic
    val GRAVITY_MULTIPLIER : Float = 0.08F
}

data class Planet(val dimension : ResourceLocation, val gravity : Float = -1F, val oxygenRate : Float = 1F, val temperature : Float) {
    fun asWorldOrNull(
        function: (ResourceKey<Level>) -> Level? = { IXplatAbstractions.INSTANCE.server()?.getLevel(it) }
    ): Level? {
        return function(ResourceKey.create(Registries.DIMENSION, dimension))
    }

    val celsius: Float get() = temperature - 273.15F
    val fahrenheit: Float get() = celsius * 1.8F + 32F
    val freezing: Boolean get() = celsius < 0F
    val modifiesGravity : Boolean get() = gravity >= 0
    val noFallDamage : Boolean get() = gameGravity < 1 && gravity > 0
    val gameGravity : Float get() = (gravity / 9.81F) * PlanetConstants.GRAVITY_MULTIPLIER
}

object PlanetRegistry {
    private val PLANETS = linkedMapOf<ResourceLocation, Planet>()

    fun register(planet: Planet) : Planet {
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

    fun all() : Collection<Planet> = PLANETS.values
    fun get(id: ResourceLocation): Planet? = PLANETS[id]
    fun get(level: Level) : Planet? = PLANETS[level.dimension().location()]

    val MARS = planet("mars") {
        gravity = 3.72F
        oxygenRate = 0F
        temperature = 210F
    }

    val MOON = planet("moon") {
        gravity = 1.62F
        oxygenRate = 0F
        temperature = 220F
    }
}

class PlanetBuilder(val id: ResourceLocation) {
    var gravity: Float = 9.81F
    var oxygenRate : Float = 1F
    var temperature: Float = 293.15F // 20 degs

    fun build() = Planet(id, gravity, oxygenRate, temperature)
}