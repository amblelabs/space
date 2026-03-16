package dev.amble.space.common.entity

import dev.amble.space.common.contraption.RocketPhysics
import dev.amble.space.common.blocks.rocket.RocketSeatBlock
import dev.amble.space.common.contraption.RocketStructure
import dev.amble.space.common.lib.SpaceEntities
import dev.amble.space.network.s2c.RocketBlocksSyncPacket
import dev.amble.space.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils.*
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData.*
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

class RocketContraptionEntity(type: EntityType<*>, level: Level) : Entity(type, level) {

    // blocks stored relative to entity origin
    val blocks = linkedMapOf<BlockPos, BlockState>()

    // physics state
    var fuelMass = 1000f
    var dryMass = 500f
    val totalMass get() = fuelMass + dryMass

    // latest rider input (written from C2S packet, consumed by server physics)
    var pitchInput = 0f
    var yawInput = 0f
    var throttleUp = false
    var throttleDown = false

    // angular velocity state for smooth/damped control response
    var pitchRate = 0f
    var yawRate = 0f

    private var lerpSteps = 0
    private var lerpX = 0.0
    private var lerpY = 0.0
    private var lerpZ = 0.0
    private var lerpYaw = 0f
    private var lerpPitch = 0f
    private var allowPassengerRemoval = false

    companion object {
        fun fromStructure(structure: RocketStructure, level: ServerLevel): RocketContraptionEntity {
            val entity = SpaceEntities.ROCKET_CONTRAPTION.create(level)!!
            // Use block-corner origin so transformed contraption blocks sit exactly on world blocks.
            entity.setPos(structure.rootPos.x.toDouble(), structure.rootPos.y.toDouble(), structure.rootPos.z.toDouble())
            structure.blocks.forEach { (worldPos, state) ->
                val relativePos = worldPos.subtract(structure.rootPos)
                entity.blocks[relativePos] = state
                level.setBlock(worldPos, Blocks.AIR.defaultBlockState(), 3)
            }
            entity.dryMass = structure.totalMass

            // start upright; physics maps neutral pitch to upward thrust.
            entity.xRot  = 0f
            entity.xRotO = 0f
            entity.yRot  = 0f
            entity.yRotO = 0f

            level.addFreshEntity(entity)
            return entity
        }


        val THROTTLE: EntityDataAccessor<Float> =
            defineId(RocketContraptionEntity::class.java, EntityDataSerializers.FLOAT)
    }

    override fun tick() {
        super.tick()
        if (level().isClientSide) {
            tickClientLerp()
            return
        }
        if (firstPassenger == null) {
            pitchInput = 0f
            yawInput = 0f
            throttleUp = false
            throttleDown = false
        }
        pushEntitiesOutOfContraption()
        if (tickCount < 3) IXplatAbstractions.INSTANCE.sendPacketTracking(this, RocketBlocksSyncPacket(id, blocks))
        RocketPhysics.tick(this, level() as ServerLevel)
    }

    override fun makeBoundingBox(): AABB = computeContraptionBounds(position())

    override fun canBeCollidedWith(): Boolean = true

    override fun isPickable(): Boolean = true

    override fun isPushable(): Boolean = true

    override fun canCollideWith(entity: Entity): Boolean = entity !is RocketContraptionEntity

    override fun interact(player: Player, hand: InteractionHand): InteractionResult {
        if (level().isClientSide) return InteractionResult.SUCCESS

        val eye = player.getEyePosition(1f)
        val look = player.getViewVector(1f)
        val end = eye.add(look.scale(6.0))
        val hit = raycastContraptionBlock(eye, end) ?: return InteractionResult.PASS

        if (hit.state.block is RocketSeatBlock) {
            if (!player.isPassenger) {
                player.startRiding(this)
            }
            return InteractionResult.SUCCESS
        }

        val use = hit.state.useWithoutItem(level(), player, BlockHitResult(hit.hitPos, hit.face, hit.worldPos, false))
        return if (use.consumesAction()) use else InteractionResult.SUCCESS
    }

    override fun removePassenger(passenger: Entity) {
        if (
            !allowPassengerRemoval &&
            !level().isClientSide &&
            passenger is Player &&
            isAlive &&
            passenger.isAlive
        ) {
            return
        }
        super.removePassenger(passenger)
    }

    override fun remove(reason: RemovalReason) {
        if (!level().isClientSide) {
            allowPassengerRemoval = true
            passengers.toList().forEach { it.stopRiding() }
        }
        super.remove(reason)
    }

    fun forceDismount(passenger: Entity) {
        if (level().isClientSide) return
        allowPassengerRemoval = true
        try {
            passenger.stopRiding()
        } finally {
            allowPassengerRemoval = false
        }
    }

    override fun lerpTo(x: Double, y: Double, z: Double, yRot: Float, xRot: Float, steps: Int) {
        lerpX = x
        lerpY = y
        lerpZ = z
        lerpYaw = yRot
        lerpPitch = xRot
        lerpSteps = maxOf(steps, 1)
    }

    fun resolveMovementAgainstWorld(move: Vec3): Vec3 {
        if (blocks.isEmpty()) return move
        var resolved = move
        if (!canTranslate(resolved.x, 0.0, 0.0)) resolved = Vec3(0.0, resolved.y, resolved.z)
        if (!canTranslate(0.0, resolved.y, 0.0)) resolved = Vec3(resolved.x, 0.0, resolved.z)
        if (!canTranslate(0.0, 0.0, resolved.z)) resolved = Vec3(resolved.x, resolved.y, 0.0)
        return resolved
    }

    private fun tickClientLerp() {
        if (lerpSteps <= 0) return
        val t = 1.0 / lerpSteps.toDouble()
        setPos(
            Mth.lerp(t, x, lerpX),
            Mth.lerp(t, y, lerpY),
            Mth.lerp(t, z, lerpZ)
        )
        yRot = Mth.rotLerp(t.toFloat(), yRot, lerpYaw)
        xRot = Mth.rotLerp(t.toFloat(), xRot, lerpPitch)
        lerpSteps--
    }

    private fun pushEntitiesOutOfContraption() {
        val bounds = computeContraptionBounds(position()).inflate(0.25)
        val nearby = level().getEntities(this, bounds) { it !in passengers }
        if (nearby.isEmpty()) return

        val blockBoxes = transformedBlockBoxes(position())
        nearby.forEach { other ->
            var otherBox = other.boundingBox
            blockBoxes.forEach { blockBox ->
                if (!otherBox.intersects(blockBox)) return@forEach
                val xPen = minOf(otherBox.maxX - blockBox.minX, blockBox.maxX - otherBox.minX)
                val yPen = minOf(otherBox.maxY - blockBox.minY, blockBox.maxY - otherBox.minY)
                val zPen = minOf(otherBox.maxZ - blockBox.minZ, blockBox.maxZ - otherBox.minZ)
                when {
                    xPen <= yPen && xPen <= zPen -> {
                        val dir = if (other.x >= blockBox.center.x) 1.0 else -1.0
                        other.setPos(other.x + xPen * dir, other.y, other.z)
                    }
                    yPen <= xPen && yPen <= zPen -> {
                        val dir = if (other.y >= blockBox.center.y) 1.0 else -1.0
                        other.setPos(other.x, other.y + yPen * dir, other.z)
                    }
                    else -> {
                        val dir = if (other.z >= blockBox.center.z) 1.0 else -1.0
                        other.setPos(other.x, other.y, other.z + zPen * dir)
                    }
                }
                otherBox = other.boundingBox
            }
        }
    }

    private fun canTranslate(dx: Double, dy: Double, dz: Double): Boolean {
        if (dx == 0.0 && dy == 0.0 && dz == 0.0) return true
        val targetOrigin = position().add(dx, dy, dz)
        // Only resolve against world blocks; rider/entity intersections should not freeze flight.
        return transformedBlockBoxes(targetOrigin).all { level().getBlockCollisions(this, it).none() }
    }

    private fun computeContraptionBounds(origin: Vec3): AABB {
        val boxes = transformedBlockBoxes(origin)
        if (boxes.isEmpty()) return AABB(origin.x - 0.5, origin.y - 0.5, origin.z - 0.5, origin.x + 0.5, origin.y + 0.5, origin.z + 0.5)
        return boxes.reduce { acc, box -> acc.minmax(box) }
    }

    private fun transformedBlockBoxes(origin: Vec3): List<AABB> {
        val blockMap = safeBlocks()
        if (blockMap.isEmpty()) return emptyList()
        return blockMap.keys.map { rel -> transformedUnitBox(rel, origin, blockMap) }
    }

    private fun safeBlocks(): Map<BlockPos, BlockState> = blocks ?: emptyMap()

    private fun transformedUnitBox(rel: BlockPos, origin: Vec3, blockMap: Map<BlockPos, BlockState>): AABB {
        var minX = Double.POSITIVE_INFINITY
        var minY = Double.POSITIVE_INFINITY
        var minZ = Double.POSITIVE_INFINITY
        var maxX = Double.NEGATIVE_INFINITY
        var maxY = Double.NEGATIVE_INFINITY
        var maxZ = Double.NEGATIVE_INFINITY
        val pivot = contraptionPivot(blockMap)

        for (dx in 0..1) {
            for (dy in 0..1) {
                for (dz in 0..1) {
                    val local = Vec3(rel.x + dx.toDouble(), rel.y + dy.toDouble(), rel.z + dz.toDouble())
                    val world = localToWorld(local, origin, pivot)
                    minX = minOf(minX, world.x)
                    minY = minOf(minY, world.y)
                    minZ = minOf(minZ, world.z)
                    maxX = maxOf(maxX, world.x)
                    maxY = maxOf(maxY, world.y)
                    maxZ = maxOf(maxZ, world.z)
                }
            }
        }

        return AABB(minX, minY, minZ, maxX, maxY, maxZ)
    }

    private data class RayHit(
        val worldPos: BlockPos,
        val state: BlockState,
        val hitPos: Vec3,
        val face: Direction,
        val distance: Double
    )

    private fun raycastContraptionBlock(startWorld: Vec3, endWorld: Vec3): RayHit? {
        val blockMap = safeBlocks()
        if (blockMap.isEmpty()) return null
        val origin = position()
        val pivot = contraptionPivot(blockMap)
        val start = worldToLocal(startWorld, origin, pivot)
        val end = worldToLocal(endWorld, origin, pivot)
        val dir = end.subtract(start)

        var best: RayHit? = null
        blockMap.forEach { (rel, state) ->
            val hit = rayBoxIntersection(start, dir, rel) ?: return@forEach
            val worldHit = localToWorld(hit.first, origin, pivot)
            val worldPos = worldHit.floor()
            val dist = startWorld.distanceToSqr(worldHit)
            if (best == null || dist < best.distance) {
                best = RayHit(worldPos, state, worldHit, hit.second, dist)
            }
        }
        return best
    }

    private fun rayBoxIntersection(start: Vec3, dir: Vec3, rel: BlockPos): Pair<Vec3, Direction>? {
        val min = Vec3(rel.x.toDouble(), rel.y.toDouble(), rel.z.toDouble())
        val max = min.add(1.0, 1.0, 1.0)

        var tMin = 0.0
        var tMax = 1.0
        var hitFace = Direction.UP

        fun axis(origin: Double, delta: Double, minVal: Double, maxVal: Double, neg: Direction, pos: Direction): Boolean {
            if (kotlin.math.abs(delta) < 1.0e-7) {
                return origin in minVal..maxVal
            }
            val inv = 1.0 / delta
            var t1 = (minVal - origin) * inv
            var t2 = (maxVal - origin) * inv
            var face = if (inv >= 0.0) neg else pos
            if (t1 > t2) {
                val tmp = t1
                t1 = t2
                t2 = tmp
                face = if (face == neg) pos else neg
            }
            if (t1 > tMin) {
                tMin = t1
                hitFace = face
            }
            if (t2 < tMax) tMax = t2
            return tMax >= tMin
        }

        if (!axis(start.x, dir.x, min.x, max.x, Direction.WEST, Direction.EAST)) return null
        if (!axis(start.y, dir.y, min.y, max.y, Direction.DOWN, Direction.UP)) return null
        if (!axis(start.z, dir.z, min.z, max.z, Direction.NORTH, Direction.SOUTH)) return null
        if (tMin < 0.0 || tMin > 1.0) return null

        return Pair(start.add(dir.scale(tMin)), hitFace)
    }

    private fun localToWorld(local: Vec3, origin: Vec3, pivot: Vec3): Vec3 {
        val centered = local.subtract(pivot)
        val pitch = xRot * Mth.DEG_TO_RAD
        val yaw = -yRot * Mth.DEG_TO_RAD
        val x1 = centered.x
        val y1 = centered.y * kotlin.math.cos(pitch.toDouble()) - centered.z * kotlin.math.sin(pitch.toDouble())
        val z1 = centered.y * kotlin.math.sin(pitch.toDouble()) + centered.z * kotlin.math.cos(pitch.toDouble())

        val x2 = x1 * kotlin.math.cos(yaw.toDouble()) + z1 * kotlin.math.sin(yaw.toDouble())
        val z2 = z1 * kotlin.math.cos(yaw.toDouble()) - x1 * kotlin.math.sin(yaw.toDouble())
        return origin.add(pivot.x + x2, pivot.y + y1, pivot.z + z2)
    }

    private fun worldToLocal(world: Vec3, origin: Vec3, pivot: Vec3): Vec3 {
        val v = world.subtract(origin).subtract(pivot)
        val yaw = yRot * Mth.DEG_TO_RAD
        val pitch = -xRot * Mth.DEG_TO_RAD

        val x1 = v.x * kotlin.math.cos(yaw.toDouble()) + v.z * kotlin.math.sin(yaw.toDouble())
        val z1 = v.z * kotlin.math.cos(yaw.toDouble()) - v.x * kotlin.math.sin(yaw.toDouble())

        val y2 = v.y * kotlin.math.cos(pitch.toDouble()) - z1 * kotlin.math.sin(pitch.toDouble())
        val z2 = v.y * kotlin.math.sin(pitch.toDouble()) + z1 * kotlin.math.cos(pitch.toDouble())
        return Vec3(x1, y2, z2).add(pivot)
    }

    private fun contraptionPivot(blockMap: Map<BlockPos, BlockState>): Vec3 {
        if (blockMap.isEmpty()) return Vec3.ZERO
        val minX = blockMap.keys.minOf { it.x.toDouble() }
        val minY = blockMap.keys.minOf { it.y.toDouble() }
        val minZ = blockMap.keys.minOf { it.z.toDouble() }
        val maxX = blockMap.keys.maxOf { it.x.toDouble() + 1.0 }
        val maxY = blockMap.keys.maxOf { it.y.toDouble() + 1.0 }
        val maxZ = blockMap.keys.maxOf { it.z.toDouble() + 1.0 }
        return Vec3((minX + maxX) * 0.5, (minY + maxY) * 0.5, (minZ + maxZ) * 0.5)
    }

    private fun Vec3.floor(): BlockPos = BlockPos.containing(x, y, z)

    override fun readAdditionalSaveData(nbt: CompoundTag) {
        fuelMass = nbt.getFloat("FuelMass")
        val blocksTag = nbt.getList("Blocks", 10)
        blocksTag.forEach { tag ->
            tag as CompoundTag
            val pos = BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"))
            val state = readBlockState(
                level().holderLookup(Registries.BLOCK),
                tag.getCompound("State")
            )
            blocks[pos] = state
        }
    }

    override fun addAdditionalSaveData(nbt: CompoundTag) {
        nbt.putFloat("FuelMass", fuelMass)
        val blocksTag = ListTag()
        blocks.forEach { (pos, state) ->
            val tag = CompoundTag()
            tag.putInt("X", pos.x)
            tag.putInt("Y", pos.y)
            tag.putInt("Z", pos.z)
            tag.put("State", writeBlockState(state))
            blocksTag.add(tag)
        }
        nbt.put("Blocks", blocksTag)
    }

    override fun defineSynchedData(builder: Builder) {
        builder.define(THROTTLE, 0f)
    }
}