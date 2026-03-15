package dev.amble.space.common.item.spacesuit

import dev.amble.space.client.renderer.SpaceSuitArmorRenderer
import net.minecraft.client.model.HumanoidModel
import net.minecraft.core.Holder
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterial
import net.minecraft.world.item.ItemStack
import software.bernie.geckolib.animatable.GeoItem
import software.bernie.geckolib.animatable.client.GeoRenderProvider
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.renderer.GeoArmorRenderer
import software.bernie.geckolib.util.GeckoLibUtil
import java.util.function.Consumer

class SpaceSuitItem(
    val variant: SpaceSuitVariant,
    armorMaterial: Holder<ArmorMaterial>,
    type: Type,
    properties: Properties
) : ArmorItem(armorMaterial, type, properties), GeoItem {
    private val cache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)

    init {
        GeoItem.registerSyncedAnimatable(this)
    }

    override fun createGeoRenderer(consumer: Consumer<GeoRenderProvider>) {
        consumer.accept(object : GeoRenderProvider {
            private var renderer: GeoArmorRenderer<*>? = null

            override fun <T : LivingEntity> getGeoArmorRenderer(
                livingEntity: T?,
                itemStack: ItemStack,
                equipmentSlot: EquipmentSlot?,
                original: HumanoidModel<T>?
            ): HumanoidModel<*> {
                if (renderer == null) {
                    renderer = SpaceSuitArmorRenderer(variant)
                }

                return renderer!!
            }
        })
    }

    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        // Placeholder for future suit animations.
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = cache

    fun Player.hasFullSpaceSuit(): Boolean = checkFullSpaceSuit(this)

    companion object {
        @JvmStatic
        fun checkFullSpaceSuit(player: Player): Boolean {
            val inv = player.inventory
            return inv.getArmor(0).item is SpaceSuitItem &&
                    inv.getArmor(1).item is SpaceSuitItem &&
                    inv.getArmor(2).item is SpaceSuitItem &&
                    inv.getArmor(3).item is SpaceSuitItem
        }
    }
}


