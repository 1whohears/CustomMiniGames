package com.onewhohears.minigames.command.all;

import com.mojang.brigadier.CommandDispatcher;
import com.onewhohears.minigames.command.GameComArgs;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class MiniGameAllCommands {
	
	public MiniGameAllCommands(CommandDispatcher<CommandSourceStack> d) {
		d.register(Commands.literal("shop").requires((stack) -> stack.hasPermission(0))
			.then(GameComArgs.enabledShopNameArgument()
			.executes(openShopComand()))
		);
		d.register(Commands.literal("kit").requires((stack) -> stack.hasPermission(0))
			.then(GameComArgs.enabledKitNameArgument()
			.executes(selectKitComand()))
		);
	}
	
	private PlayerAgentsCommand openShopComand() {
		return (context, agents) -> {
			// TODO 3.4.2 let normal players open shops
			context.getSource().sendFailure(Component.literal("This doesn't do notn yet L!"));
			return 1;
		};
	}
	
	private PlayerAgentsCommand selectKitComand() {
		return (context, agents) -> {
			// TODO 3.4.2 let normal players select kits
			context.getSource().sendFailure(Component.literal("This doesn't do notn yet L!"));
			return 1;
		};
	}
	
}
