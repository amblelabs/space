package dev.amble.lib.datagen

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class NoBlockDrop(val silkTouch : Boolean = false, val slabDrops : Boolean = false)