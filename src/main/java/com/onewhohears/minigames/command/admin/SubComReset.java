package com.onewhohears.minigames.command.admin;

import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SubComReset {
	
	public SubComReset() {
	}
	
	public ArgumentBuilder<CommandSourceStack,?> reset() {
		return Commands.literal("reset")
			.then(GameDataCom.runningGameIdArgument()
				.then(Commands.literal("confirm_reset")
				.executes(commandReset()))
			);
	}
	
	private GameDataCom commandReset() {
		return (context, gameData) -> {
			gameData.reset(context.getSource().getServer());
			Component message = Component.literal(gameData.getInstanceId()+" was reset!");
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
}
