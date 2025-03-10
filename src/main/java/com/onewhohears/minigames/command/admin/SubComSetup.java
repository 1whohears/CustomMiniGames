package com.onewhohears.minigames.command.admin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.onewhohears.minigames.command.GameComArgs;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.agent.TeamAgent;

import com.onewhohears.minigames.minigame.param.MiniGameParamType;
import com.onewhohears.onewholibs.util.UtilMCText;
import com.onewhohears.onewholibs.util.math.UtilGeometry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;

import static com.onewhohears.minigames.minigame.param.MiniGameParamTypes.DEFAULT_LIVES;

public class SubComSetup {
	
	public SubComSetup() {
	}
	
	public ArgumentBuilder<CommandSourceStack,?> setup() {
		ArgumentBuilder<CommandSourceStack,?> runningGamesArg = getRunningGamesArg();
		for (MiniGameParamType<?> type : MiniGameManager.getGameParamTypes())
			runningGamesArg.then(type.getCommandArgument());
        return Commands.literal("setup").then(runningGamesArg);
	}

	private ArgumentBuilder<CommandSourceStack,?> getRunningGamesArg() {
		return GameComArgs.runningGameIdArgument()
				.then(Commands.literal("start").executes(commandStartGame()))
				.then(addTeamArg()).then(removeTeamArg())
				.then(addPlayerArg()).then(removePlayerArg())
				.then(setSpawnPlayerArg())
				.then(setSpawnTeamArg())
				.then(setLivesArg())
				.then(randomizeTeamsArg());
	}

	private ArgumentBuilder<CommandSourceStack,?> randomizeTeamsArg() {
		return Commands.literal("randomize_teams")
				.executes(commandRandomizeTeam());
	}

	private ArgumentBuilder<CommandSourceStack,?> addTeamArg() {
		return Commands.literal("add_team")
				.then(Commands.argument("team", TeamArgument.team())
				.executes(commandAddTeam()));
	}
	
	private ArgumentBuilder<CommandSourceStack,?> removeTeamArg() {
		return Commands.literal("remove_team")
				.then(Commands.argument("team", TeamArgument.team())
				.executes(commandRemoveTeam()));
	}
	
	private ArgumentBuilder<CommandSourceStack,?> addPlayerArg() {
		return Commands.literal("add_player")
				.then(Commands.argument("player", EntityArgument.players())
				.executes(commandAddPlayers()));
	}
	
	private ArgumentBuilder<CommandSourceStack,?> removePlayerArg() {
		return Commands.literal("remove_player")
				.then(Commands.argument("player", EntityArgument.players())
				.executes(commandRemovePlayers()));
	}
	
	private ArgumentBuilder<CommandSourceStack,?> setSpawnPlayerArg() {
		return Commands.literal("set_spawn_player")
				.then(Commands.argument("player", EntityArgument.players())
					.then(Commands.argument("spawn_pos", BlockPosArgument.blockPos())
					.executes(commandSetPlayerSpawn())));
	}

	private ArgumentBuilder<CommandSourceStack,?> setSpawnTeamArg() {
		return Commands.literal("set_spawn_team")
					.then(Commands.argument("team", TeamArgument.team())
							.then(Commands.argument("spawn_pos", BlockPosArgument.blockPos())
									.executes(commandSetTeamSpawn())));
	}
	
	private ArgumentBuilder<CommandSourceStack,?> setLivesArg() {
		return Commands.literal("set_lives")
				.then(Commands.argument("lives", IntegerArgumentType.integer(1))
						.executes(commandSetLives())
						.then(Commands.literal("player")
								.then(Commands.argument("players", EntityArgument.players())
										.suggests(GameComArgs.suggestPlayerAgentNames())
										.executes(commandSetLivesPlayers())
								)
						)
						.then(Commands.literal("team")
								.then(Commands.argument("team", TeamArgument.team())
										.suggests(GameComArgs.suggestTeamAgentNames())
										.executes(commandSetLivesTeam())
								)
						)
				);
	}

	private GameSetupCom commandRandomizeTeam() {
		return (context, gameData) -> {
			List<TeamAgent> teams = gameData.getTeamAgents();
			Collections.shuffle(teams);
			List<PlayerAgent> randomPlayers = gameData.getAllPlayerAgents();
			Collections.shuffle(randomPlayers);
			int k = 0;
			for (PlayerAgent agent : randomPlayers) {
				ServerPlayer player = agent.getPlayer(context.getSource().getServer());
				if (player == null) continue;
				TeamAgent team = teams.get(k);
				team.addPlayer(context.getSource().getServer(), player);
				++k;
				if (k >= teams.size()) k = 0;
			}
			return 1;
		};
	}

	private GameSetupCom commandSetLives() {
		return (context, gameData) -> {
			int lives = IntegerArgumentType.getInteger(context, "lives");
			gameData.setParam(DEFAULT_LIVES, lives);
			gameData.setAllAgentInitialLives(lives);
			Component message = UtilMCText.literal("Set "+gameData.getInstanceId()+" default initial lives to "+lives);
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}

	private GameSetupCom commandSetLivesPlayers() {
		return (context, gameData) -> {
			int lives = IntegerArgumentType.getInteger(context, "lives");
			Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
			for (ServerPlayer player : players) {
				PlayerAgent agent = gameData.getPlayerAgentByUUID(player.getStringUUID());
				if (agent == null) {
					Component message = UtilMCText.literal("Player "+player.getScoreboardName()+" is not in game "+gameData.getInstanceId());
					context.getSource().sendFailure(message);
					continue;
				}
				agent.setInitialLives(lives);
			}
			Component message = UtilMCText.literal("Set initial lives to "+lives);
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}

	private GameSetupCom commandSetLivesTeam() {
		return (context, gameData) -> {
			int lives = IntegerArgumentType.getInteger(context, "lives");
			PlayerTeam team = TeamArgument.getTeam(context, "team");
			TeamAgent agent = gameData.getTeamAgentByName(team.getName());
			if (agent == null) {
				Component message = UtilMCText.literal("Team "+team.getName()+" is not in game "+gameData.getInstanceId());
				context.getSource().sendFailure(message);
				return 0;
			}
			agent.setInitialLives(lives);
			Component message = UtilMCText.literal("Set initial lives to "+lives);
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
	private GameSetupCom commandSetPlayerSpawn() {
		return (context, gameData) -> {
			Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "player");
			if (players.isEmpty()) {
				Component message = Component.literal("No player spawnpoints were changed");
				context.getSource().sendSuccess(message, true);
				return 1;
			}
			BlockPos pos = BlockPosArgument.getSpawnablePos(context, "spawn_pos");
			for (ServerPlayer player : players) {
				PlayerAgent agent = gameData.getPlayerAgentByUUID(player.getStringUUID());
				if (agent == null) {
					Component message = Component.literal("The player ").append(player.getDisplayName())
							.append(" is not in the game "+gameData.getInstanceId());
					context.getSource().sendFailure(message);
					continue;
				}
				agent.setRespawnPoint(UtilGeometry.toVec3(pos));
				Component message = Component.literal("Set ").append(player.getDisplayName())
						.append(" spawn point to "+pos.toShortString());
				context.getSource().sendSuccess(message, true);
			}
			return 1;
		};
	}
	
	private GameSetupCom commandSetTeamSpawn() {
		return (context, gameData) -> {
			PlayerTeam team = TeamArgument.getTeam(context, "team");
			BlockPos pos = BlockPosArgument.getSpawnablePos(context, "spawn_pos");
			TeamAgent agent = gameData.getTeamAgentByName(team.getName());
			if (agent == null) {
				Component message = Component.literal("The team ").append(team.getDisplayName())
						.append(" is not in the game "+gameData.getInstanceId());
				context.getSource().sendFailure(message);
				return 0;
			}
			agent.setRespawnPoint(UtilGeometry.toVec3(pos));
			Component message = Component.literal("Set ").append(team.getDisplayName())
					.append(" spawn point to "+pos.toShortString());
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
	private GameSetupCom commandStartGame() {
		return (context, gameData) -> {
			String reason  = gameData.getStartFailedReason(context.getSource().getServer());
			if (reason != null) {
				MutableComponent message = Component.literal(gameData.getInstanceId()+" is currently unable to finish setup!");
				message.append("\nUse /minigame setup "+gameData.getInstanceId()+" to finish setting up the game.");
				message.append("\n"+reason);
				context.getSource().sendFailure(message);
				return 0;
			}
			Component message = Component.literal("Starting "+gameData.getInstanceId()+"!");
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
	private GameSetupCom commandRemovePlayers() {
		return (context, gameData) -> {
			Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "player");
			if (players.isEmpty()) {
				context.getSource().sendSuccess(Component.literal("No players could be removed."), true);
				return 1;
			}
			for (ServerPlayer player : players) {
				if (gameData.removeAgentById(player.getStringUUID())) {
					Component message = Component.literal("Failed to remove ").append(player.getDisplayName())
							.append(" from "+gameData.getInstanceId()+"!");
					context.getSource().sendFailure(message);
				} else {
					Component message = Component.literal("Removed player ").append(player.getDisplayName())
							.append(" from "+gameData.getInstanceId()+"!");
					context.getSource().sendSuccess(message, true);
				}
			}
			return 1;
		};
	}
	
	private GameSetupCom commandAddPlayers() {
		return (context, gameData) -> {
			if (!gameData.canAddIndividualPlayers()) {
				Component message = Component.literal("The game instance "+gameData.getInstanceId()+" does not allow individual players!");
				context.getSource().sendFailure(message);
				return 0;
			}
			Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "player");
			if (players.isEmpty()) {
				context.getSource().sendSuccess(Component.literal("No players could be added."), true);
				return 1;
			}
			for (ServerPlayer player : players) {
				if (gameData.getAddIndividualPlayer(player) == null) {
					Component message = Component.literal("Failed to add ").append(player.getDisplayName())
							.append(" to "+gameData.getInstanceId()+"!");
					context.getSource().sendFailure(message);
				} else {
					Component message = Component.literal("Added player ").append(player.getDisplayName())
							.append(" to "+gameData.getInstanceId()+"!");
					context.getSource().sendSuccess(message, true);
				}
			}
			return 1;
		};
	}
	
	private GameSetupCom commandRemoveTeam() {
		return (context, gameData) -> {
			PlayerTeam team = TeamArgument.getTeam(context, "team");
			if (!gameData.removeAgentById(team.getName())) {
				Component message = Component.literal("Already removed "+team.getName()+" from "+gameData.getInstanceId()+"!");
				context.getSource().sendSuccess(message, true);
				return 1;
			}
			MutableComponent message = Component.literal("Removed "+team.getName()+" from "+gameData.getInstanceId()+"!");
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
	private GameSetupCom commandAddTeam() {
		return (context, gameData) -> {
			if (!gameData.canAddTeams()) {
				Component message = Component.literal("The game instance "+gameData.getInstanceId()+" does not allow teams!");
				context.getSource().sendFailure(message);
				return 0;
			}
			PlayerTeam team = TeamArgument.getTeam(context, "team");
			if (gameData.getAddTeam(team) == null) {
				Component message = Component.literal("Failed to add "+team.getName()+" to "+gameData.getInstanceId()+"!");
				context.getSource().sendFailure(message);
				return 0;
			}
			MutableComponent message = Component.literal("Added team "+team.getName()+" to "+gameData.getInstanceId()+"!");
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
}
