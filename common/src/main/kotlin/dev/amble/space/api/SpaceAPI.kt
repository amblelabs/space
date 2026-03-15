package dev.amble.space.api

import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object SpaceAPI {
    const val MOD_ID: String = "space"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(MOD_ID)

    @JvmStatic
    fun modLoc(path: String): ResourceLocation =
        ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
}
