package com.onewhohears.minigames.command.all;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class MiniGameAllCommands {
	
	public MiniGameAllCommands(CommandDispatcher<CommandSourceStack> d) {
		d.register(Commands.literal("openshop").requires((stack) -> stack.hasPermission(0))
			// TODO 3.4.2 let normal players open shops
			.executes((context) -> {
				context.getSource().sendFailure(Component.literal("This doesn't do notn yet L!"));
				return 1;
			})
		);
	}
	
}
