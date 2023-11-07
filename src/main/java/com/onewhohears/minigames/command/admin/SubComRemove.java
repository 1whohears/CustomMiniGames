package com.onewhohears.minigames.command.admin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.onewhohears.minigames.minigame.MiniGameManager;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SubComRemove {
	
	public SubComRemove() {
	}
	
	public ArgumentBuilder<CommandSourceStack,?> remove() {
		return Commands.literal("remove")
			.then(GameDataCom.runningGameIdArgument()
				.then(Commands.literal("confirm_remove")
				.executes(commandRemove()))
			);
	}
	
	private GameDataCom commandRemove() {
		return (context, gameData) -> {
			gameData.reset(context.getSource().getServer());
			MiniGameManager.get().removeGame(gameData.getInstanceId());
			Component message = Component.literal(gameData.getInstanceId()+" was removed!");
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
}
