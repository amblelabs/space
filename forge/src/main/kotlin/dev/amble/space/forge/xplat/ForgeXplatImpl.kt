package dev.amble.space.forge.xplat

import com.google.common.base.Suppliers
import dev.amble.space.api.SpaceAPI
import dev.amble.space.api.mod.SpaceTags
import dev.amble.space.xplat.IXplatAbstractions
import dev.amble.space.xplat.IXplatTags
import dev.amble.space.xplat.Platform
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientCommonPacketListener
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
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
import net.neoforged.fml.ModList
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.FluidType
import net.neoforged.neoforge.fluids.capability.IFluidHandler
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.server.ServerLifecycleHooks
import java.util.function.BiFunction

class ForgeXplatImpl : IXplatAbstractions {
    override fun platform() = Platform.FORGE
    override fun isPhysicalClient() = FMLEnvironment.dist.isClient
    override fun isModPresent(id: String) = ModList.get().isLoaded(id)

    override fun initPlatformSpecific() {
        // add optional mod interop here
    }

    override fun server(): MinecraftServer? = ServerLifecycleHooks.getCurrentServer()

    override fun sendPacketToPlayer(target: ServerPlayer, packet: CustomPacketPayload) =
        PacketDistributor.sendToPlayer(target, packet)

    override fun sendPacketNear(pos: Vec3, radius: Double, dimension: ServerLevel, packet: CustomPacketPayload) =
        PacketDistributor.sendToPlayersNear(dimension, null, pos.x, pos.y, pos.z, radius, packet)

    override fun sendPacketTracking(entity: Entity, packet: CustomPacketPayload) =
        PacketDistributor.sendToPlayersTrackingEntity(entity, packet)

    override fun sendToAll(packet: CustomPacketPayload) {
        PacketDistributor.sendToAllPlayers(packet)
    }

    override fun toVanilla(message: CustomPacketPayload): Packet<ClientCommonPacketListener> =
        ClientboundCustomPayloadPacket(message)

    override fun <T : BlockEntity> createBlockEntityType(func: BiFunction<BlockPos, BlockState, T>, vararg blocks: Block): BlockEntityType<T> =
        BlockEntityType.Builder.of(func::apply, *blocks).build(null)

    override fun tryPlaceFluid(level: Level, hand: InteractionHand, pos: BlockPos, fluid: Fluid): Boolean {
        val handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.UP) ?: return false
        return handler.fill(FluidStack(fluid, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE) > 0
    }

    override fun drainAllFluid(level: Level, pos: BlockPos): Boolean {
        val handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.UP) ?: return false
        var any = false

        for (tank in 0 until handler.tanks) {
            val contained = handler.getFluidInTank(tank)
            if (!contained.isEmpty) {
                val drained = handler.drain(contained.copy(), IFluidHandler.FluidAction.EXECUTE)
                if (!drained.isEmpty) any = true
            }
        }

        return any
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
        return ModList.get().getModContainerById(namespace)
            .map { it.modInfo.displayName }
            .orElse(namespace)
    }

    override fun isDev() = !FMLEnvironment.production
    override fun isUnstable(): Boolean = UNSTABLE.get()

    companion object {
        private val TAGS = object : IXplatTags {}
        private val UNSTABLE = Suppliers.memoize {
            !ModList.get().getModContainerById(SpaceAPI.MOD_ID)
                .orElseThrow().modInfo.version.toString().contains("release")
        }
    }
}

