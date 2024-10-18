package com.onewhohears.minigames.command.all;

import java.util.List;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public interface PlayerAgentsCommand extends Command<CommandSourceStack> {
	
	default int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayer();
		if (player == null) {
			Component message = Component.literal("This command must be used by a player!");
			context.getSource().sendFailure(message);
			return 0;
		}
		List<PlayerAgent<?>> agents = MiniGameManager.get().getActiveGamePlayerAgents(player);
		if (agents.isEmpty()) {
			Component message = Component.literal("You must be in an active game to use this command!");
			context.getSource().sendFailure(message);
			return 0;
		}
		return runPlayerAgents(context, agents);
	}
	
	int runPlayerAgents(CommandContext<CommandSourceStack> context, List<PlayerAgent<?>> agents) throws CommandSyntaxException;
	
}
