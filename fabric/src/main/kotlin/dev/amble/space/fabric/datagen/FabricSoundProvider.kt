package dev.amble.space.fabric.datagen

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.amble.space.api.SpaceAPI
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.sounds.SoundEvent
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.stream.Stream

class FabricSoundProvider(
    private val packOutput: PackOutput,
    private val modId: String,
    private val extractVariants: Boolean = true
) : DataProvider {

    private val sounds: MutableMap<String, MutableSet<SoundEventWrapper>> = HashMap()

    private fun isValidName(name: String): Boolean {
        if (name.contains(" ")) {
            SpaceAPI.LOGGER.error("Sound event name cannot contain spaces: {}", name)
            return false
        }
        if (name.any { it.isUpperCase() }) {
            SpaceAPI.LOGGER.error("Sound event name cannot contain capital letters: {}", name)
            return false
        }
        return true
    }

    fun addSound(name: String, validate: Boolean = true, vararg events: SoundEvent) {
        if (validate && !isValidName(name)) return
        val set = sounds.getOrPut(name) { HashSet() }
        events.forEach { set.add(SoundEventWrapper(it)) }
    }

    override fun run(output: CachedOutput): CompletableFuture<*> {
        getSoundsFromMod(modId).forEach { sound ->
            val path = sound.location.path
            addSound(path, validate = false, sound)

            if (extractVariants) {
                val newPath = extractPath(path) ?: return@forEach
                addSound(newPath, validate = false, sound)
            }
        }

        val soundsJson = JsonObject()
        sounds.forEach { (soundName, soundEvents) ->
            soundsJson.add(soundName, serializeSounds(soundEvents))
        }

        return DataProvider.saveStable(output, soundsJson, getOutputPath())
    }

    fun getOutputPath(): Path =
        packOutput.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
            .resolve(modId)
            .resolve("sounds.json")
    override fun getName(): String = "Sound Definitions"

    private fun serializeSounds(wrappers: Iterable<SoundEventWrapper>): JsonObject {
        val obj = JsonObject()
        val soundsArray = JsonArray()
        wrappers.forEach { soundsArray.add(it.event.location.toString()) }
        obj.add("sounds", soundsArray)
        return obj
    }

    private fun extractPath(path: String): String? {
        val cursor = StringCursor(path, path.length - 1, -1)
        while (true) {
            if (!cursor.peek().isDigit()) return cursor.substring()
            cursor.next()
        }
    }

    fun getSoundsFromMod(namespace: String): Stream<SoundEvent> =
        BuiltInRegistries.SOUND_EVENT.stream()
            .filter { it.location.namespace == namespace }

    class SoundEventWrapper(val event: SoundEvent) {
        override fun hashCode(): Int = event.location.hashCode()
        override fun equals(other: Any?): Boolean =
            other is SoundEventWrapper && other.event.location == event.location
    }
}

class StringCursor(private val str: String, private var cursor: Int, private val step: Int) {
    fun next() { cursor += step }
    fun peek(): Char = str[cursor]
    fun peekNext(): Char = str[cursor + step]

    fun substring(): String? = when {
        step > 0 -> if (cursor == 0) null else str.substring(cursor)
        step < 0 -> if (cursor + 1 == str.length) null else str.take(cursor + 1)
        else -> null
    }
}