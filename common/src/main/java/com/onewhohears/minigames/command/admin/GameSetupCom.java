package com.onewhohears.minigames.command.admin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.onewhohears.minigames.minigame.data.MiniGameData;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public interface GameSetupCom extends GameDataCom {
	
	default int runGameData(CommandContext<CommandSourceStack> context, MiniGameData gameData) throws CommandSyntaxException {
		if (!gameData.isSetupPhase()) {
			Component message = Component.literal("The game instance "+gameData.getInstanceId()
					+" is not in the setup phase! You must reset (Dangerous!)");
			context.getSource().sendFailure(message);
			return 0;
		}
		return runSetup(context, gameData);
	}
	
	int runSetup(CommandContext<CommandSourceStack> context, MiniGameData gameData) throws CommandSyntaxException;
	
}
