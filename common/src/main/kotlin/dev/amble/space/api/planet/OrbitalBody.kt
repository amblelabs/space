package dev.amble.space.api.planet

import net.minecraft.resources.ResourceLocation

data class OrbitalBody(
    val id: ResourceLocation,
    val mass: Double,
    val radius: Double,
) {
    val planet: Planet? get() = PlanetRegistry.get(id)
}

data class OrbitalElement(
    val body: OrbitalBody,
    val parentId: ResourceLocation?,
    val semiMajorAxis: Double,
    val eccentricity: Double,
    val inclination: Float,
    val epochAngle: Float,
    val children: List<OrbitalElement> = emptyList()
)

data class SolarSystem(
    val id: ResourceLocation, val starId: ResourceLocation, val starMass: Double, val starRadius: Double, val elements: List<OrbitalElement>
) {
    fun findElement(id: ResourceLocation): OrbitalElement? = elements.flattenTree().find { it.body.id == id }

    fun allElements(): List<OrbitalElement> = elements.flattenTree()
}

fun List<OrbitalElement>.flattenTree(): List<OrbitalElement> = flatMap { listOf(it) + it.children.flattenTree() }