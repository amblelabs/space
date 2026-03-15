package dev.amble.space.client.model

import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.builders.LayerDefinition
import java.util.function.BiConsumer
import java.util.function.Supplier

@Suppress("unused")
object SpaceModelLayers {

    @JvmStatic
    fun init(consumer: BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>>) {
        // register model layers here
    }

    private fun make(name: String, layer: String = "main") =
        ModelLayerLocation(modLoc(name), layer)
}

