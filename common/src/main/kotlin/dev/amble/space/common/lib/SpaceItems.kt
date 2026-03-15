package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import java.util.function.BiConsumer

@Suppress("unused", "MemberVisibilityCanBePrivate")
object SpaceItems {
    private val ITEMS = linkedMapOf<ResourceLocation, Item>()
    private val ITEM_TABS = linkedMapOf<CreativeModeTab, MutableList<ItemEntry>>()

    @JvmStatic
    fun registerItems(r: BiConsumer<Item, ResourceLocation>) =
        ITEMS.forEach { (k, v) -> r.accept(v, k) }

    @JvmStatic
    fun registerItemCreativeTab(r: CreativeModeTab.Output, tab: CreativeModeTab) =
        ITEM_TABS.getOrDefault(tab, emptyList()).forEach { it.register(r) }

    @JvmStatic fun props(): Item.Properties = Item.Properties()
    @JvmStatic fun unstackable(): Item.Properties = props().stacksTo(1)

    fun <T : Item> make(id: String, item: T, tab: CreativeModeTab? = SpaceCreativeTabs.SPACE): T {
        val old = ITEMS.put(modLoc(id), item)
        check(old == null) { "Duplicate id $id" }
        if (tab != null) ITEM_TABS.getOrPut(tab) { mutableListOf() }.add(ItemEntry(item))
        return item
    }

    private class ItemEntry(private val item: Item) {
        fun register(r: CreativeModeTab.Output) = r.accept(item)
    }
}

