package dev.amble.space.api.planet

import dev.amble.space.api.SpaceAPI
import dev.amble.space.network.s2c.SolarSystemSyncPacket
import dev.amble.space.xplat.IXplatAbstractions
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.saveddata.SavedData

class SolarSystemSavedData : SavedData() {

    var elapsedSeconds: Double = 0.0
        private set
    var timeScale: Double = 1.0
        private set

    // How many ticks between syncs to clients
    private var ticksSinceSync = 0
    private val syncInterval = 100  // every 5 seconds

    fun tick(server: MinecraftServer) {
        elapsedSeconds += (1.0 / 20.0) * timeScale
        setDirty()

        ticksSinceSync++
        if (ticksSinceSync >= syncInterval) {
            ticksSinceSync = 0
            syncToAll(server)
        }
    }

    fun setTimeScale(scale: Double, server: MinecraftServer) {
        timeScale = scale.coerceAtLeast(0.0)
        setDirty()
        syncToAll(server)  // sync immediately on change
    }

    fun fastForward(seconds: Double, server: MinecraftServer) {
        elapsedSeconds += seconds
        setDirty()
        syncToAll(server)  // sync immediately on change
    }

    fun reset(server: MinecraftServer) {
        elapsedSeconds = 0.0
        timeScale = 1.0
        setDirty()
        syncToAll(server)
    }

    fun syncToPlayer(player: net.minecraft.server.level.ServerPlayer) {
        IXplatAbstractions.INSTANCE.sendPacketToPlayer(player, SolarSystemSyncPacket(elapsedSeconds, timeScale))
    }

    private fun syncToAll(server: MinecraftServer) {
        val packet = SolarSystemSyncPacket(elapsedSeconds, timeScale)
        IXplatAbstractions.INSTANCE.sendToAll(packet)
    }

    override fun save(tag: CompoundTag, provider: HolderLookup.Provider): CompoundTag {
        tag.putDouble("elapsed", elapsedSeconds)
        tag.putDouble("timeScale", timeScale)
        return tag
    }

    companion object {
        private val FACTORY = Factory(
            ::SolarSystemSavedData,
            { tag, provider ->
                SolarSystemSavedData().apply {
                    elapsedSeconds = tag.getDouble("elapsed")
                    timeScale = tag.getDouble("timeScale")
                }
            },
            null
        )

        fun get(server: MinecraftServer): SolarSystemSavedData =
            server.overworld().dataStorage.computeIfAbsent(FACTORY, "space_solar_system")
    }
}