package dev.amble.space.interop

object SpaceInterop {
    const val PATCHOULI_ANY_INTEROP_FLAG = "space:any_interop"

    @JvmStatic
    fun init() {
        // platform-independent interop init
    }

    @JvmStatic
    fun clientInit() {
        // client-side interop init
    }

    object Fabric {
        const val TRINKETS_API_ID = "trinkets"
    }

    object Forge {
        const val CURIOS_API_ID = "curios"
    }
}

