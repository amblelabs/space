package dev.amble.space.fabric

import dev.amble.space.api.mod.SpaceStatistics
import dev.amble.space.common.blocks.behavior.SpaceComposting
import dev.amble.space.common.blocks.behavior.SpaceStrippable
import dev.amble.space.common.lib.*
import dev.amble.space.interop.SpaceInterop
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.state.properties.BlockSetType
import java.util.function.BiConsumer

class FabricSpaceInit : ModInitializer {

    override fun onInitialize() {
        initListeners()
        initRegistries()

        SpaceComposting.setup()
        SpaceStrippable.init()
        SpaceInterop.init()
    }

    private fun initListeners() {
        CommandRegistrationCallback.EVENT.register { dp, _, _ -> SpaceCommands.register(dp) }

        ItemGroupEvents.MODIFY_ENTRIES_ALL.register { tab, entries ->
            SpaceBlocks.registerBlockCreativeTab(entries::accept, tab)
            SpaceItems.registerItemCreativeTab(entries, tab)
        }
    }

    private fun initRegistries() {
        SpaceBlockSetTypes.registerBlocks(BlockSetType::register)
        SpaceCreativeTabs.registerCreativeTabs(bind(BuiltInRegistries.CREATIVE_MODE_TAB))
        SpaceSounds.registerSounds(bind(BuiltInRegistries.SOUND_EVENT))
        SpaceBlocks.forEach(bind(BuiltInRegistries.BLOCK))
        SpaceBlocks.registerBlockItems(bind(BuiltInRegistries.ITEM))
        SpaceBlockEntities.registerTiles(bind(BuiltInRegistries.BLOCK_ENTITY_TYPE))
        SpaceItems.registerItems(bind(BuiltInRegistries.ITEM))
        SpaceEntities.registerEntities(bind(BuiltInRegistries.ENTITY_TYPE))
        SpaceAttributes.register(bind(BuiltInRegistries.ATTRIBUTE))
        SpaceMobEffects.register(bind(BuiltInRegistries.MOB_EFFECT))
        SpacePotions.register(bind(BuiltInRegistries.POTION))
        SpaceComponents.registerComponents(bind(BuiltInRegistries.DATA_COMPONENT_TYPE))
        SpaceParticles.registerParticles(bind(BuiltInRegistries.PARTICLE_TYPE))
        SpaceLootFunctions.registerSerializers(bind(BuiltInRegistries.LOOT_FUNCTION_TYPE))
        @Suppress("UnusedExpression") FlammableBlockRegistry.getDefaultInstance()
        SpaceStatistics.register()
    }

    private fun <T> bind(registry: Registry<T>): BiConsumer<T, ResourceLocation> =
        BiConsumer { t, id ->
            if (t != null) {
                Registry.register(registry, id, t)
            }
        }
}


