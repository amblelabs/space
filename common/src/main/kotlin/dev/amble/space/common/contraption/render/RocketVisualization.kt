package dev.amble.space.common.contraption.render

import dev.amble.space.common.lib.SpaceEntities
import dev.engine_room.flywheel.api.visualization.VisualizationManager
import dev.engine_room.flywheel.lib.visualization.SimpleEntityVisualizer

object RocketVisualization {
    @JvmStatic
    fun register() {
        SimpleEntityVisualizer.builder(SpaceEntities.ROCKET_CONTRAPTION)
            .factory(::RocketVisual)
            .skipVanillaRender { VisualizationManager.supportsVisualization(it.level()) }
            .apply()
    }
}