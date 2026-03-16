package dev.amble.space.client.renderer.space

import dev.amble.space.api.planet.Planet
import dev.amble.space.api.planet.PlanetRegistry
import net.minecraft.client.Minecraft

object AtmosphereRenderer {
    fun currentPlanet() = Minecraft.getInstance().level?.let { PlanetRegistry.get(it) }
}