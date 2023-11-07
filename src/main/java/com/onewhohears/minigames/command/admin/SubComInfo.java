package com.onewhohears.minigames.command.admin;

import java.util.Arrays;
import java.util.List;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class SubComInfo {
	
	public ArgumentBuilder<CommandSourceStack,?> info() {
		return Commands.literal("info")
			.then(listRunning())
			.then(GameDataCom.runningGameIdArgument()
				.then(Commands.literal("list_players").executes(commandPlayerList()))
			);
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
			for (PlayerAgent<?> agent : players) {
				ServerPlayer sp = agent.getPlayer(context.getSource().getServer());
				if (sp == null) message.append(agent.getId());
				else message.append(sp.getDisplayName());
				message.append(", ");
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
