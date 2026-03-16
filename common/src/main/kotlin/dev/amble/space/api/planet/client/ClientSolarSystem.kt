package dev.amble.space.api.planet.client

object ClientSolarSystem {

    var elapsedSeconds: Double = 0.0
        private set
    var timeScale: Double = 1.0
        private set

    // Server's last known elapsed at the time of sync, used to detect drift
    private var serverElapsed: Double = 0.0
    private var serverTimeScale: Double = 1.0

    // Called every client tick to interpolate between server syncs
    fun tick() {
        elapsedSeconds += (1.0 / 20.0) * timeScale
    }

    // Called when a sync packet arrives
    fun onSync(serverElapsedSeconds: Double, serverTimeScaleValue: Double) {
        serverElapsed = serverElapsedSeconds
        serverTimeScale = serverTimeScaleValue
        timeScale = serverTimeScaleValue
        elapsedSeconds = serverElapsedSeconds
    }

    // Call on player join / world load to ensure clean state
    fun reset() {
        elapsedSeconds = 0.0
        timeScale = 1.0
        serverElapsed = 0.0
        serverTimeScale = 1.0
    }
}