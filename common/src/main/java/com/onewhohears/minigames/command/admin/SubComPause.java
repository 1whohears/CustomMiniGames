package com.onewhohears.minigames.command.admin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.onewhohears.minigames.command.GameComArgs;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SubComPause {

	public SubComPause() {
	}
	
	public ArgumentBuilder<CommandSourceStack,?> pause() {
		return Commands.literal("pause")
			.then(GameComArgs.runningGameIdArgument()
				.executes(commandPause())
			);
	}
	
	private GameDataCom commandPause() {
		return (context, gameData) -> {
			if (gameData.pause(context.getSource().getServer())) {
				Component message = Component.literal(gameData.getInstanceId() + " was paused!");
				context.getSource().sendSuccess(message, true);
				return 1;
			} else {
				Component message = Component.literal(gameData.getInstanceId() + " cannot be paused!");
				context.getSource().sendFailure(message);
				return 0;
			}
		};
	}
	
}
