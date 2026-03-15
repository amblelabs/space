package dev.amble.space.api.mod

import dev.amble.space.api.SpaceAPI.modLoc
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

object SpaceTags {
    object Items {
        @JvmStatic fun create(name: String): TagKey<Item> = create(modLoc(name))
        @JvmStatic fun create(id: ResourceLocation): TagKey<Item> = TagKey.create(Registries.ITEM, id)
    }

    object Blocks {
        @JvmStatic fun create(name: String): TagKey<Block> = TagKey.create(Registries.BLOCK, modLoc(name))
    }

    object Entities {
        @JvmStatic fun create(name: String): TagKey<EntityType<*>> = TagKey.create(Registries.ENTITY_TYPE, modLoc(name))
    }
}

