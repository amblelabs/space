package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SpriteSet
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.resources.ResourceLocation
import java.util.function.BiConsumer
import java.util.function.Function

@Suppress("unused")
object SpaceParticles {
    private val PARTICLES = linkedMapOf<ResourceLocation, ParticleType<*>>()

    @JvmStatic
    fun registerParticles(r: BiConsumer<ParticleType<*>, ResourceLocation>) =
        PARTICLES.forEach { (k, v) -> r.accept(v, k) }

    private fun <O : ParticleOptions, T : ParticleType<O>> register(id: String, particle: T): T {
        val old = PARTICLES.put(modLoc(id), particle)
        check(old == null) { "Typo? Duplicate id $id" }
        return particle
    }

    object FactoryHandler {
        fun interface Consumer<T : ParticleOptions> {
            fun register(type: ParticleType<T>, constructor: Function<SpriteSet, ParticleProvider<T>>)
        }

        @Suppress("EmptyFunctionBlock")
        @JvmStatic
        fun <T : ParticleOptions> registerFactories(consumer: Consumer<T>) {}
    }
}

