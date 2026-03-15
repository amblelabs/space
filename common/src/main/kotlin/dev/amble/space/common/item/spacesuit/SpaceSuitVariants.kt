package dev.amble.space.common.item.spacesuit

import java.util.Collections

object SpaceSuitVariants {
    private val variants = linkedMapOf<String, SpaceSuitVariant>()

    @JvmField
    val STANDARD: SpaceSuitVariant = register("standard")

    @JvmStatic
    fun register(id: String): SpaceSuitVariant {
        val variant = SpaceSuitVariant(id)
        val previous = variants.put(id, variant)
        check(previous == null) { "Duplicate spacesuit variant id: $id" }
        return variant
    }

    @JvmStatic
    fun get(id: String): SpaceSuitVariant? = variants[id]

    @JvmStatic
    fun all(): Collection<SpaceSuitVariant> = Collections.unmodifiableCollection(variants.values)
}

