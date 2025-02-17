package com.onewhohears.minigames.command.admin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.onewhohears.minigames.command.GameComArgs;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SubComResume {

	public SubComResume() {
	}
	
	public ArgumentBuilder<CommandSourceStack,?> resume() {
		return Commands.literal("resume")
			.then(GameComArgs.runningGameIdArgument()
				.executes(commandResume())
			);
	}
	
	private GameDataCom commandResume() {
		return (context, gameData) -> {
			if (gameData.resume(context.getSource().getServer())) {
				Component message = Component.literal(gameData.getInstanceId() + " was resumed!");
				context.getSource().sendSuccess(message, true);
				return 1;
			} else {
				Component message = Component.literal(gameData.getInstanceId() + " cannot be resumed!");
				context.getSource().sendFailure(message);
				return 0;
			}
		};
	}
	
}
