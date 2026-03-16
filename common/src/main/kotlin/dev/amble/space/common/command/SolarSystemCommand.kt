package dev.amble.space.common.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import dev.amble.space.api.planet.SolarSystemSavedData
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object SolarSystemCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("space")
                .requires { it.hasPermission(2) }

                .then(Commands.literal("timescale")
                    .then(Commands.argument("scale", DoubleArgumentType.doubleArg(0.0))
                        .executes { ctx ->
                            val scale = DoubleArgumentType.getDouble(ctx, "scale")
                            SolarSystemSavedData.get(ctx.source.server).setTimeScale(scale, ctx.source.server)
                            ctx.source.sendSuccess(
                                { Component.literal("Time scale set to ${scale}x") }, true
                            )
                            1
                        }
                    )
                )

                .then(Commands.literal("fastforward")
                    .then(Commands.argument("seconds", DoubleArgumentType.doubleArg(0.0))
                        .executes { ctx ->
                            val seconds = DoubleArgumentType.getDouble(ctx, "seconds")
                            SolarSystemSavedData.get(ctx.source.server).fastForward(seconds, ctx.source.server)
                            ctx.source.sendSuccess(
                                { Component.literal("Advanced orbits by ${seconds}s (${formatTime(seconds)})") }, true
                            )
                            1
                        }
                    )
                )

                .then(Commands.literal("fastforward")
                    .then(Commands.literal("days")
                        .then(Commands.argument("days", DoubleArgumentType.doubleArg(0.0))
                            .executes { ctx ->
                                val days = DoubleArgumentType.getDouble(ctx, "days")
                                val seconds = days * 86_400.0
                                SolarSystemSavedData.get(ctx.source.server).fastForward(seconds, ctx.source.server)
                                ctx.source.sendSuccess(
                                    { Component.literal("Advanced orbits by $days days") }, true
                                )
                                1
                            }
                        )
                    )
                )

                .then(Commands.literal("status")
                    .executes { ctx ->
                        val data = SolarSystemSavedData.get(ctx.source.server)
                        ctx.source.sendSuccess({
                            Component.literal(
                                "Elapsed: ${formatTime(data.elapsedSeconds)} | Time scale: ${data.timeScale}x"
                            )
                        }, false)
                        1
                    }
                )

                .then(Commands.literal("reset")
                    .executes { ctx ->
                        SolarSystemSavedData.get(ctx.source.server).reset(ctx.source.server)
                        ctx.source.sendSuccess(
                            { Component.literal("Orbits reset to epoch") }, true
                        )
                        1
                    }
                )
        )
    }

    private fun formatTime(seconds: Double): String {
        val days = (seconds / 86_400).toLong()
        val hours = ((seconds % 86_400) / 3_600).toLong()
        val mins = ((seconds % 3_600) / 60).toLong()
        return "${days}d ${hours}h ${mins}m"
    }
}