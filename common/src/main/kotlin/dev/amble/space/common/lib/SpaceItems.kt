package dev.amble.space.common.lib

import dev.amble.space.api.SpaceAPI.modLoc
import dev.amble.space.common.item.spacesuit.SpaceSuitItem
import dev.amble.space.common.item.spacesuit.SpaceSuitVariant
import dev.amble.space.common.item.spacesuit.SpaceSuitVariants
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterials
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import java.util.EnumMap
import java.util.function.Consumer
import java.util.function.BiConsumer

@Suppress("unused", "MemberVisibilityCanBePrivate")
object SpaceItems {
    private val ITEMS = linkedMapOf<ResourceLocation, Item>()
    private val ITEM_TABS = linkedMapOf<CreativeModeTab, MutableList<ItemEntry>>()

    @JvmField
    val STANDARD_SPACESUIT: Map<ArmorItem.Type, SpaceSuitItem> = registerSuit("spacesuit", SpaceSuitVariants.STANDARD)

    @JvmStatic
    fun registerItems(r: BiConsumer<Item, ResourceLocation>) =
        ITEMS.forEach { (k, v) -> r.accept(v, k) }

    @JvmStatic
    fun forEachItemId(consumer: Consumer<ResourceLocation>) =
        ITEMS.keys.forEach(consumer::accept)

    @JvmStatic
    fun registerItemCreativeTab(r: CreativeModeTab.Output, tab: CreativeModeTab) =
        ITEM_TABS.getOrDefault(tab, emptyList()).forEach { it.register(r) }

    @JvmStatic fun props(): Item.Properties = Item.Properties()
    @JvmStatic fun unstackable(): Item.Properties = props().stacksTo(1)

    @JvmStatic
    fun registerSuit(baseName: String, variant: SpaceSuitVariant, tab: CreativeModeTab? = SpaceCreativeTabs.SPACE): Map<ArmorItem.Type, SpaceSuitItem> {
        val pieces = EnumMap<ArmorItem.Type, SpaceSuitItem>(ArmorItem.Type::class.java)

        pieces[ArmorItem.Type.HELMET] = make(
            "${variant.id}_${baseName}_helmet",
            SpaceSuitItem(variant, ArmorMaterials.IRON, ArmorItem.Type.HELMET, unstackable()),
            tab
        )
        pieces[ArmorItem.Type.CHESTPLATE] = make(
            "${variant.id}_${baseName}_chestplate",
            SpaceSuitItem(variant, ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE, unstackable()),
            tab
        )
        pieces[ArmorItem.Type.LEGGINGS] = make(
            "${variant.id}_${baseName}_leggings",
            SpaceSuitItem(variant, ArmorMaterials.IRON, ArmorItem.Type.LEGGINGS, unstackable()),
            tab
        )
        pieces[ArmorItem.Type.BOOTS] = make(
            "${variant.id}_${baseName}_boots",
            SpaceSuitItem(variant, ArmorMaterials.IRON, ArmorItem.Type.BOOTS, unstackable()),
            tab
        )

        return pieces
    }

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

