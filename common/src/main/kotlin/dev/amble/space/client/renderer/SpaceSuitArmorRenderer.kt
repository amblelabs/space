package dev.amble.space.client.renderer

import dev.amble.space.api.SpaceAPI
import dev.amble.space.common.item.spacesuit.SpaceSuitItem
import dev.amble.space.common.item.spacesuit.SpaceSuitVariant
import software.bernie.geckolib.model.DefaultedItemGeoModel
import software.bernie.geckolib.renderer.GeoArmorRenderer

class SpaceSuitArmorRenderer(variant: SpaceSuitVariant) : GeoArmorRenderer<SpaceSuitItem>(DefaultedItemGeoModel(SpaceAPI.modLoc("armor/" + variant.id + "_spacesuit")))

