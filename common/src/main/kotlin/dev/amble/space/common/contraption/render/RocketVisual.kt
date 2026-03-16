package dev.amble.space.common.contraption.render

import dev.amble.space.common.entity.RocketContraptionEntity
import dev.engine_room.flywheel.api.task.Plan
import dev.engine_room.flywheel.api.visual.DynamicVisual
import dev.engine_room.flywheel.api.visual.LightUpdatedVisual
import dev.engine_room.flywheel.api.visual.SectionTrackedVisual
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.instance.InstanceTypes
import dev.engine_room.flywheel.lib.instance.TransformedInstance
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder
import dev.engine_room.flywheel.lib.task.SimplePlan
import dev.engine_room.flywheel.lib.task.functional.RunnableWithContext
import dev.engine_room.flywheel.lib.visual.AbstractEntityVisual
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.util.Mth
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.phys.Vec3

class RocketVisual(
    ctx: VisualizationContext,
    entity: RocketContraptionEntity,
    partialTick: Float
) : AbstractEntityVisual<RocketContraptionEntity>(ctx, entity, partialTick),
    DynamicVisual,
    LightUpdatedVisual {
    private data class BlockEntry(
        val instance: TransformedInstance,
        val relPos: BlockPos
    )

    private val blockEntries = mutableListOf<BlockEntry>()
    private var sectionCollector: SectionTrackedVisual.SectionCollector? = null

    // called once after construction — replaces our old init()
    override fun update(partialTick: Float) {
        if (blockEntries.isNotEmpty()) return  // already initialised

        val mc = Minecraft.getInstance()

        entity.blocks.forEach { (relPos, state) ->
            val bakedModel = mc.blockRenderer.blockModelShaper.getBlockModel(state)
            val model = BakedModelBuilder(bakedModel)
                .level(entity.level() as BlockAndTintGetter)
                .pos(entity.blockPosition())
                .build()

            val instance = instancerProvider()
                .instancer(InstanceTypes.TRANSFORMED, model)
                .createInstance()

            blockEntries += BlockEntry(instance, relPos)
        }

        updateLight(partialTick)
        updateSections()
    }

    override fun planFrame(): Plan<DynamicVisual.Context> =
        SimplePlan.of(RunnableWithContext { context ->
            updateSections()
            if (!isVisible(context.frustum())) {
                blockEntries.forEach { it.instance.setZeroTransform().setChanged() }
                return@RunnableWithContext
            }

            val pt = context.partialTick()
            val visualPos = getVisualPosition(pt)
            val yaw   = Mth.lerp(pt, entity.yRotO, entity.yRot)
            val xRot = Mth.lerp(pt, entity.xRotO, entity.xRot)
            val pivot = blockPivot()
            blockEntries.forEach { (instance, relPos) ->
                instance.setIdentityTransform()
                    .translate(visualPos.x, visualPos.y, visualPos.z)
                    .translate(pivot.x, pivot.y, pivot.z)
                    .rotateY(-yaw * Mth.DEG_TO_RAD)
                    .rotateX(xRot * Mth.DEG_TO_RAD)
                    .translate(-pivot.x, -pivot.y, -pivot.z)
                    .translate(relPos.x.toFloat(), relPos.y.toFloat(), relPos.z.toFloat())
                    .setChanged()
            }
        })

    // LightUpdatedVisual
    override fun updateLight(partialTick: Float) {
        relight(partialTick, *blockEntries.map { it.instance }.toTypedArray())
    }

    // SectionTrackedVisual — Flywheel needs to know which sections this visual occupies
    // so it knows when to call updateLight
    override fun setSectionCollector(collector: SectionTrackedVisual.SectionCollector) {
        sectionCollector = collector
        updateSections()
    }

    private fun updateSections() {
        val minSectionY = entity.level().minSection
        val maxSectionYExclusive = entity.level().maxSection + 1
        sectionCollector?.sections(
            LongOpenHashSet(
                entity.blocks.keys
                    .map { relPos ->
                        SectionPos.of(
                            BlockPos(
                                (entity.x + relPos.x).toInt(),
                                (entity.y + relPos.y).toInt(),
                                (entity.z + relPos.z).toInt()
                            )
                        )
                    }
                    .filter { section -> section.y in minSectionY until maxSectionYExclusive }
                    .map { it.asLong() }
            )
        )
    }

    override fun _delete() {
        blockEntries.forEach { it.instance.delete() }
        blockEntries.clear()
    }

    private fun blockPivot(): Vec3 {
        if (entity.blocks.isEmpty()) return Vec3.ZERO
        val minX = entity.blocks.keys.minOf { it.x }
        val minY = entity.blocks.keys.minOf { it.y }
        val minZ = entity.blocks.keys.minOf { it.z }
        val maxX = entity.blocks.keys.maxOf { it.x + 1 }
        val maxY = entity.blocks.keys.maxOf { it.y + 1 }
        val maxZ = entity.blocks.keys.maxOf { it.z + 1 }
        return Vec3(
            (minX + maxX) * 0.5,
            (minY + maxY) * 0.5,
            (minZ + maxZ) * 0.5
        )
    }
}