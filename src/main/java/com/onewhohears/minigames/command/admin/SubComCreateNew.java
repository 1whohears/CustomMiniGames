package com.onewhohears.minigames.command.admin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.onewhohears.minigames.command.GameComArgs;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.data.MiniGameData;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SubComCreateNew {
	
	public SubComCreateNew() {
	}
	
	public ArgumentBuilder<CommandSourceStack,?> createNew() {
		return Commands.literal("create_new")
			.then(GameComArgs.gameTypeIdArgument()
				.then(Commands.argument("instance_id", StringArgumentType.word())
				.executes(commandCreateNew()))
			);
	}
	
	private Command<CommandSourceStack> commandCreateNew() {
		return (context) -> {
			try {
				String gameTypeId = StringArgumentType.getString(context, "game_type");
				if (!MiniGameManager.hasGameType(gameTypeId)) {
					Component message = Component.literal("The Game Type " + gameTypeId + " does not exist!");
					context.getSource().sendFailure(message);
					return 0;
				}
				String gameInstanceId = StringArgumentType.getString(context, "instance_id");
				if (MiniGameManager.get().isGameRunning(gameInstanceId)) {
					Component message = Component.literal(gameInstanceId + " already exists! You may want to reset or remove it.");
					context.getSource().sendFailure(message);
					return 0;
				}
				MiniGameData gameData = MiniGameManager.get().startNewGame(gameTypeId, gameInstanceId);
				if (gameData == null) {
					Component message = Component.literal("Unable to start new game " + gameInstanceId + " of type " + gameTypeId);
					context.getSource().sendFailure(message);
					return 0;
				}
				gameData.setGameCenter(context.getSource().getPosition());
				MutableComponent message = Component.literal("Started new game of type " + gameTypeId + " called " + gameInstanceId + ".");
				message.append("\nUse /game setup " + gameInstanceId + " to configure the game.");
				message.append("\n" + gameData.getSetupInfo());
				context.getSource().sendSuccess(message, true);
				return 1;
			} catch (Exception e) {
				Component message = Component.literal("Unable to start new game due to error: "+e.getMessage());
				context.getSource().sendFailure(message);
				e.printStackTrace();
			}
			return 0;
		};
	}
	
}
