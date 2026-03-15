package dev.amble.space.forge

import com.google.common.base.Suppliers
import dev.amble.space.api.SpaceAPI
import dev.amble.space.api.mod.SpaceStatistics
import dev.amble.space.common.blocks.behavior.SpaceComposting
import dev.amble.space.common.blocks.behavior.SpaceStrippable
import dev.amble.space.common.lib.*
import dev.amble.space.interop.SpaceInterop
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegisterEvent
import java.util.function.BiConsumer

object ForgeSpace {
    val ITEMS: DeferredRegister<Item> = DeferredRegister.create(BuiltInRegistries.ITEM, SpaceAPI.MOD_ID)
    private var initialized = false

    fun init(modBus: IEventBus) {
        if (initialized) return
        initialized = true

        modBus.addListener(this::onRegister)
        modBus.addListener(this::onBuildCreativeModeTabContents)
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands)

        SpaceBlockSetTypes.registerBlocks(BlockSetType::register)
        SpaceComposting.setup()
        SpaceStrippable.init()
        SpaceInterop.init()
        SpaceStatistics.register()
    }

    private fun onRegister(event: RegisterEvent) {
        register(event, BuiltInRegistries.CREATIVE_MODE_TAB, SpaceCreativeTabs::registerCreativeTabs)
        register(event, BuiltInRegistries.SOUND_EVENT, SpaceSounds::registerSounds)
        register(event, BuiltInRegistries.BLOCK, SpaceBlocks::registerBlocks)
        register(event, BuiltInRegistries.ITEM, SpaceBlocks::registerBlockItems)
        register(event, BuiltInRegistries.BLOCK_ENTITY_TYPE, SpaceBlockEntities::registerTiles)
        register(event, BuiltInRegistries.ENTITY_TYPE, SpaceEntities::registerEntities)
        register(event, BuiltInRegistries.ATTRIBUTE, SpaceAttributes::register)
        register(event, BuiltInRegistries.MOB_EFFECT, SpaceMobEffects::register)
        register(event, BuiltInRegistries.POTION, SpacePotions::register)
        register(event, BuiltInRegistries.DATA_COMPONENT_TYPE, SpaceComponents::registerComponents)
        register(event, BuiltInRegistries.PARTICLE_TYPE, SpaceParticles::registerParticles)
        register(event, BuiltInRegistries.LOOT_FUNCTION_TYPE, SpaceLootFunctions::registerSerializers)
    }

    private fun onBuildCreativeModeTabContents(event: BuildCreativeModeTabContentsEvent) {
        SpaceBlocks.registerBlockCreativeTab({ block -> event.accept(block) }, event.tab)
        SpaceItems.registerItemCreativeTab(event, event.tab)
    }

    private fun onRegisterCommands(event: RegisterCommandsEvent) {
        SpaceCommands.register(event.dispatcher)
    }

    private fun <T : Any> register(
        event: RegisterEvent,
        registry: Registry<T>,
        registrar: (BiConsumer<T, ResourceLocation>) -> Unit
    ) {
        event.register(registry.key()) { helper ->
            registrar(BiConsumer { value, id -> helper.register(id, value) })
        }
    }
}

