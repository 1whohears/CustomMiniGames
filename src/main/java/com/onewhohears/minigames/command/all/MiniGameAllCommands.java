package com.onewhohears.minigames.command.all;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class MiniGameAllCommands {
	
	public MiniGameAllCommands(CommandDispatcher<CommandSourceStack> d) {
		d.register(Commands.literal("gameshop").requires(null)
			
		);
	}
	
}
