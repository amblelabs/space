package dev.amble.space.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity

class EmptyEntityRenderer<T : Entity>(
    ctx: EntityRendererProvider.Context
) : EntityRenderer<T>(ctx) {
    override fun getTextureLocation(entity: T): ResourceLocation =
        ResourceLocation.withDefaultNamespace("textures/misc/white.png")

    override fun render(
        entity: T, yaw: Float, partialTick: Float,
        stack: PoseStack, buffer: MultiBufferSource, light: Int
    ) { /* Flywheel handles this */ }
}