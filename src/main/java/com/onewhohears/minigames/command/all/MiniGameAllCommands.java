package com.onewhohears.minigames.command.all;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class MiniGameAllCommands {
	
	public MiniGameAllCommands(CommandDispatcher<CommandSourceStack> d) {
		d.register(Commands.literal("gameshop").requires((stack) -> stack.hasPermission(0))
			.executes((context) -> {
				context.getSource().sendFailure(Component.literal("This doesn't do notn yet L!"));
				return 1;
			})
		);
	}
	
}
