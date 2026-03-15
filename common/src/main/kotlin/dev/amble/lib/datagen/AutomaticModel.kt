package dev.amble.lib.datagen

/**
 * - No annotation? both
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class AutomaticModel(val type: Type = Type.ALL)

enum class Type {
    ALL,
    BLOCK,
    ITEM,
    NONE
}
