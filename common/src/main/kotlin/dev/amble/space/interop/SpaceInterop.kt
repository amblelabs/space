package dev.amble.space.interop

import dev.amble.space.xplat.IXplatAbstractions

object SpaceInterop {
    const val PATCHOULI_ANY_INTEROP_FLAG = "space:any_interop"

    @JvmStatic
    fun init() {
        // Ensure platform-specific lifecycle hooks (like Fabric server capture) are registered.
        IXplatAbstractions.INSTANCE.initPlatformSpecific()
    }

    object Fabric {
        const val TRINKETS_API_ID = "trinkets"
    }

    object Forge {
        const val CURIOS_API_ID = "curios"
    }
}

