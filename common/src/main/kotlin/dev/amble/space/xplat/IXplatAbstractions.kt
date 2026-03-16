package dev.amble.space.xplat

import dev.amble.space.api.SpaceAPI
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientCommonPacketListener
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.phys.Vec3
import java.util.ServiceLoader
import java.util.function.BiFunction

@Suppress("unused")
interface IXplatAbstractions {
    fun platform(): Platform
    fun isModPresent(id: String): Boolean
    fun getModName(namespace: String): String
    fun isDev(): Boolean
    fun isUnstable(): Boolean
    fun isPhysicalClient(): Boolean
    fun initPlatformSpecific()
    fun server(): MinecraftServer?

    fun sendPacketToPlayer(target: ServerPlayer, packet: CustomPacketPayload)
    fun sendPacketNear(pos: Vec3, radius: Double, dimension: ServerLevel, packet: CustomPacketPayload)
    fun sendPacketTracking(entity: Entity, packet: CustomPacketPayload)
    fun sendToAll(packet: CustomPacketPayload)
    fun toVanilla(message: CustomPacketPayload): Packet<ClientCommonPacketListener>

    fun <T : BlockEntity> createBlockEntityType(func: BiFunction<BlockPos, BlockState, T>, vararg blocks: Block): BlockEntityType<T>
    fun tryPlaceFluid(level: Level, hand: InteractionHand, pos: BlockPos, fluid: Fluid): Boolean
    fun drainAllFluid(level: Level, pos: BlockPos): Boolean

    fun tags(): IXplatTags
    fun isShearsCondition(): LootItemCondition.Builder

    companion object {
        val INSTANCE: IXplatAbstractions = run {
            val providers = ServiceLoader.load(IXplatAbstractions::class.java).stream().toList()
            check(providers.size == 1) {
                val names = providers.joinToString(",", "[", "]") { it.type().name }
                "There should be exactly one IXplatAbstractions implementation on the classpath. Found: $names"
            }
            val provider = providers.first()
            SpaceAPI.LOGGER.debug("Instantiating xplat impl: {}", provider.type().name)
            provider.get()
        }
    }
}
