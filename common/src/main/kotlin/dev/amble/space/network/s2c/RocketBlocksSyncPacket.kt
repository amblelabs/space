package dev.amble.space.network.s2c

import dev.amble.space.api.SpaceAPI
import dev.amble.space.common.entity.RocketContraptionEntity
import dev.amble.space.network.SpacePackets
import dev.engine_room.flywheel.api.visualization.VisualizationManager
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

data class RocketBlocksSyncPacket(
    val entityId: Int,
    val blocks: Map<BlockPos, BlockState>
) : CustomPacketPayload {

    override fun type() = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<RocketBlocksSyncPacket>(
            SpaceAPI.modLoc("rocket_blocks_sync")
        )

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, RocketBlocksSyncPacket> =
            StreamCodec.ofMember(
                { packet, buf ->
                    buf.writeInt(packet.entityId)
                    buf.writeInt(packet.blocks.size)
                    packet.blocks.forEach { (pos, state) ->
                        buf.writeBlockPos(pos)
                        buf.writeInt(Block.getId(state))
                    }
                },
                { buf ->
                    val entityId = buf.readInt()
                    val count = buf.readInt()
                    val blocks = linkedMapOf<BlockPos, BlockState>()
                    repeat(count) {
                        blocks[buf.readBlockPos()] = Block.stateById(buf.readInt())
                    }
                    RocketBlocksSyncPacket(entityId, blocks)
                }
            )

        fun handle(packet: RocketBlocksSyncPacket, ctx: SpacePackets.PacketContext) {
            ctx.enqueue {
                val level = ctx.level() ?: return@enqueue
                val entity = level.getEntity(packet.entityId)
                        as? RocketContraptionEntity ?: return@enqueue
                entity.blocks.clear()
                entity.blocks.putAll(packet.blocks)
                VisualizationManager.get(level)?.entities()?.queueUpdate(entity)
            }
        }
    }
}