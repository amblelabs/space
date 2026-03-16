package dev.amble.space.common.lib

import com.mojang.brigadier.CommandDispatcher
import dev.amble.space.common.command.SolarSystemCommand
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object SpaceCommands {
    @JvmStatic
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val mainCmd = Commands.literal("space")
        // register sub-commands here
        dispatcher.register(mainCmd)
        SolarSystemCommand.register(dispatcher)
    }
}

