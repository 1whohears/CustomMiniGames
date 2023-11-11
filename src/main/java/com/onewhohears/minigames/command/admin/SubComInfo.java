package com.onewhohears.minigames.command.admin;

import java.util.Arrays;
import java.util.List;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.onewhohears.minigames.command.GameComArgs;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.agent.TeamAgent;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SubComInfo {
	
	public ArgumentBuilder<CommandSourceStack,?> info() {
		return Commands.literal("info")
			.then(listRunning())
			.then(GameComArgs.runningGameIdArgument().executes(commandgameInfo())
				.then(Commands.literal("list_players").executes(commandPlayerList()))
				.then(Commands.literal("list_teams").executes(commandTeamList()))
			);
	}
	
	private GameDataCom commandgameInfo() {
		return (context, gameData) -> {
			CommandSourceStack source = context.getSource();
			source.sendSuccess(gameData.getDebugInfo(source.getServer()), true);
			return 1;
		};
	}
	
	private GameDataCom commandPlayerList() {
		return (context, gameData) -> {
			List<PlayerAgent<?>> players = gameData.getAllPlayerAgents();
			if (players.size() == 0) {
				Component message = Component.literal("There are zero players in the game "+gameData.getInstanceId());
				context.getSource().sendSuccess(message, true);
				return 1;
			}
			MutableComponent message = Component.empty();
			for (int i = 0; i < players.size(); ++i) {
				PlayerAgent<?> agent = players.get(i);
				if (i != 0) message.append(", ");
				message.append(agent.getDebugInfo(context.getSource().getServer()));
			}
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
	private GameDataCom commandTeamList() {
		return (context, gameData) -> {
			List<TeamAgent<?>> teams = gameData.getTeamAgents();
			if (teams.size() == 0) {
				Component message = Component.literal("There are zero teams in the game "+gameData.getInstanceId());
				context.getSource().sendSuccess(message, true);
				return 1;
			}
			MutableComponent message = Component.empty();
			for (int i = 0; i < teams.size(); ++i) {
				TeamAgent<?> agent = teams.get(i);
				if (i != 0) message.append(", ");
				message.append(agent.getDebugInfo(context.getSource().getServer()));
			}
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
	private ArgumentBuilder<CommandSourceStack,?> listRunning() {
		return Commands.literal("list_running").executes((context) -> {
			String[] ids = MiniGameManager.get().getRunningeGameIds();
			Component message;
			if (ids.length == 0) message = Component.literal("There are currently no games running.");
			else message = Component.literal(Arrays.deepToString(ids));
			context.getSource().sendSuccess(message, true);
			return 1;
		});
	}
	
}
