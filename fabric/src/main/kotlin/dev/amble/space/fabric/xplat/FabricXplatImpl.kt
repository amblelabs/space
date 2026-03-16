package dev.amble.space.fabric.xplat

import com.google.common.base.Suppliers
import dev.amble.space.api.SpaceAPI
import dev.amble.space.api.mod.SpaceTags
import dev.amble.space.xplat.IXplatAbstractions
import dev.amble.space.xplat.IXplatTags
import dev.amble.space.xplat.Platform
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientCommonPacketListener
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.predicates.MatchTool
import net.minecraft.world.phys.Vec3
import java.util.function.BiFunction
import java.util.Optional

class FabricXplatImpl : IXplatAbstractions {
    private var server: MinecraftServer? = null

    override fun platform() = Platform.FABRIC
    override fun isPhysicalClient() = FabricLoader.getInstance().environmentType == EnvType.CLIENT
    override fun isModPresent(id: String) = FabricLoader.getInstance().isModLoaded(id)

    override fun initPlatformSpecific() {
        // add optional mod interop here

        ServerLifecycleEvents.SERVER_STARTING.register { server1 -> server = server1 }
        ServerLifecycleEvents.SERVER_STOPPED.register { _ -> server = null }
    }

    override fun server(): MinecraftServer? = server

    override fun sendPacketToPlayer(target: ServerPlayer, packet: CustomPacketPayload) =
        ServerPlayNetworking.send(target, packet)

    override fun sendPacketNear(pos: Vec3, radius: Double, dimension: ServerLevel, packet: CustomPacketPayload) =
        sendPacketToPlayers(PlayerLookup.around(dimension, pos, radius), packet)

    override fun sendPacketTracking(entity: Entity, packet: CustomPacketPayload) =
        sendPacketToPlayers(PlayerLookup.tracking(entity), packet)

    override fun sendToAll(packet: CustomPacketPayload) {
        server()?.let { sendPacketToPlayers(it.playerList.players, packet) }
    }

    private fun sendPacketToPlayers(players: Collection<ServerPlayer>, packet: CustomPacketPayload) {
        val pkt = toVanilla(packet)
        players.forEach { it.connection.send(pkt) }
    }

    override fun toVanilla(message: CustomPacketPayload): Packet<ClientCommonPacketListener> =
        ServerPlayNetworking.createS2CPacket(message)

    @Suppress("DEPRECATION")
    override fun <T : BlockEntity> createBlockEntityType(func: BiFunction<BlockPos, BlockState, T>, vararg blocks: Block): BlockEntityType<T> =
        FabricBlockEntityTypeBuilder.create(func::apply, *blocks).build()

    override fun tryPlaceFluid(level: Level, hand: InteractionHand, pos: BlockPos, fluid: Fluid): Boolean {
        val target = FluidStorage.SIDED.find(level, pos, Direction.UP) ?: return false
        Transaction.openOuter().use { tx ->
            val inserted = target.insert(FluidVariant.of(fluid), FluidConstants.BUCKET, tx)
            if (inserted > 0) {
                tx.commit()
                return true
            }
        }
        return false
    }

    override fun drainAllFluid(level: Level, pos: BlockPos): Boolean {
        val target = FluidStorage.SIDED.find(level, pos, Direction.UP) ?: return false
        Transaction.openOuter().use { tx ->
            var any = false
            for (view in target) {
                if (view.extract(view.resource, view.amount, tx) > 0) any = true
            }
            if (any) {
                tx.commit()
                return true
            }
        }
        return false
    }

    override fun tags(): IXplatTags = TAGS

    override fun isShearsCondition(): LootItemCondition.Builder = AnyOfCondition.anyOf(
        MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS)),
        MatchTool.toolMatches(ItemPredicate.Builder.item().of(
            SpaceTags.Items.create(ResourceLocation.fromNamespaceAndPath("c", "shears"))
        ))
    )

    override fun getModName(namespace: String): String {
        if (namespace == "c") return "Common"
        return FabricLoader.getInstance().getModContainer(namespace)
            .map { it.metadata.name }
            .orElse(namespace)
    }

    override fun isDev() = FabricLoader.getInstance().isDevelopmentEnvironment
    override fun isUnstable() = UNSTABLE.get()

    companion object {
        private val TAGS = object : IXplatTags {}
        private val UNSTABLE = Suppliers.memoize {
            !FabricLoader.getInstance().getModContainer(SpaceAPI.MOD_ID)
                .orElseThrow().metadata.version.friendlyString.contains("release")
        }
    }
}
