package dev.amble.space.api.mod

import dev.amble.space.api.SpaceAPI
import net.minecraft.resources.ResourceLocation

object SpaceConfig {

    interface CommonConfigAccess

    interface ClientConfigAccess

    interface ServerConfigAccess

    fun anyMatch(keys: List<String>, key: ResourceLocation): Boolean =
        keys.any { ResourceLocation.tryParse(it) == key }

    fun noneMatch(keys: List<String>, key: ResourceLocation): Boolean =
        !anyMatch(keys, key)

    fun anyMatchResLoc(keys: List<ResourceLocation>, key: ResourceLocation): Boolean =
        keys.any { it == key }

    private var common: CommonConfigAccess? = null
    private var client: ClientConfigAccess? = null
    private var server: ServerConfigAccess? = null

    fun common(): CommonConfigAccess = checkNotNull(common) { "accessed config too early" }
    fun client(): ClientConfigAccess = checkNotNull(client) { "accessed config too early" }
    fun server(): ServerConfigAccess = checkNotNull(server) { "accessed config too early" }

    @JvmStatic fun setCommon(access: CommonConfigAccess) {
        common?.let { SpaceAPI.LOGGER.warn("CommonConfigAccess was replaced! Old {} New {}", it.javaClass.name, access.javaClass.name) }
        common = access
    }

    @JvmStatic fun setClient(access: ClientConfigAccess) {
        client?.let { SpaceAPI.LOGGER.warn("ClientConfigAccess was replaced! Old {} New {}", it.javaClass.name, access.javaClass.name) }
        client = access
    }

    @JvmStatic fun setServer(access: ServerConfigAccess) {
        server?.let { SpaceAPI.LOGGER.warn("ServerConfigAccess was replaced! Old {} New {}", it.javaClass.name, access.javaClass.name) }
        server = access
    }
}

