package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI
import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.function.BiConsumer

object SpaceCreativeTabs {
    private val TABS = linkedMapOf<ResourceLocation, CreativeModeTab>()

    @JvmField
    val SPACE: CreativeModeTab = register(
        "main",
        CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
            .icon { ItemStack(Items.NETHER_STAR) }
    )

    @JvmStatic
    fun registerCreativeTabs(r: BiConsumer<CreativeModeTab, ResourceLocation>) =
        TABS.forEach { (k, v) -> r.accept(v, k) }

    private fun register(name: String, builder: CreativeModeTab.Builder): CreativeModeTab {
        val tab = builder.title(Component.translatable("itemGroup.${SpaceAPI.MOD_ID}.$name")).build()
        val old = TABS.put(modLoc(name), tab)
        check(old == null) { "Typo? Duplicate id $name" }
        return tab
    }
}

