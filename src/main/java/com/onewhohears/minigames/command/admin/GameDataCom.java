package com.onewhohears.minigames.command.admin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.data.MiniGameData;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public interface GameDataCom extends Command<CommandSourceStack> {
	
	default int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		String gameInstanceId = StringArgumentType.getString(context, "instance_id");
		if (!MiniGameManager.get().isGameRunning(gameInstanceId)) {
			Component message = Component.literal("The game instance "+gameInstanceId+" does not exist!");
			context.getSource().sendFailure(message);
			return 0;
		}
		return runGameData(context, MiniGameManager.get().getRunningGame(gameInstanceId));
	}
	
	int runGameData(CommandContext<CommandSourceStack> context, MiniGameData gameData) throws CommandSyntaxException;
	
}
