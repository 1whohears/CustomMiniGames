package com.onewhohears.minigames.command.admin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.onewhohears.minigames.command.GameComArgs;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public class SubComOverride {

	public SubComOverride() {
	}
	
	public ArgumentBuilder<CommandSourceStack,?> override() {
		return Commands.literal("override")
				.then(Commands.literal("set_lives")
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("lives", IntegerArgumentType.integer(0))
									.executes(setLives()))));
	}

	private Command<CommandSourceStack> setLives() {
		return (context) -> {
			Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
			int lives = IntegerArgumentType.getInteger(context, "lives");
			for (ServerPlayer player : players) {
				List<PlayerAgent> agents = MiniGameManager.get().getActiveGamePlayerAgents(player);
				if (agents.isEmpty()) {
					Component message = Component.literal(player.getScoreboardName()+" is not in any active games!");
					context.getSource().sendFailure(message);
					continue;
				}
				for (PlayerAgent agent : agents) {
					agent.setLives(lives);
					Component message = Component.literal("Set "+player.getScoreboardName()+"'s lives to "+lives+"!");
					context.getSource().sendSuccess(message, true);
				}
			}
			return 1;
		};
	}
}
