package com.onewhohears.minigames.command.admin;

import java.util.Collection;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.onewhohears.minigames.command.GameComArgs;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.agent.TeamAgent;

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

public class SubComSetup {
	
	public SubComSetup() {
	}
	
	public ArgumentBuilder<CommandSourceStack,?> setup() {
		return Commands.literal("setup")
				.then(GameComArgs.runningGameIdArgument()
								.then(Commands.literal("start").executes(commandStartGame()))
								.then(addTeamArg()).then(removeTeamArg())
								.then(addPlayerArg()).then(removePlayerArg())
								.then(setCenterArg())
								.then(setSizeArg())
								.then(setSpawnArg())
								.then(setLivesArg())
								.then(setUseBorderArg())
								.then(setClearOnStartArg())
								// TODO 3.4.4 add remove shops command
								// TODO 3.5.4 add remove kits command
			);
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
	
	private ArgumentBuilder<CommandSourceStack,?> setCenterArg() {
		return Commands.literal("set_center")
				.then(Commands.argument("game_center", BlockPosArgument.blockPos())
				.executes(commandSetCenter()));
	}
	
	private ArgumentBuilder<CommandSourceStack,?> setSizeArg() {
		return Commands.literal("set_size")
				.then(Commands.argument("game_size", DoubleArgumentType.doubleArg(-5.9999968E7D, 5.9999968E7D))
				.executes(commandSetSize()));
	}
	
	private ArgumentBuilder<CommandSourceStack,?> setSpawnArg() {
		return Commands.literal("set_spawn")
				.then(Commands.argument("player", EntityArgument.players())
					.then(Commands.argument("spawn_pos", BlockPosArgument.blockPos())
					.executes(commandSetPlayerSpawn())))
				.then(Commands.argument("team", TeamArgument.team())
					.then(Commands.argument("spawn_pos", BlockPosArgument.blockPos())
					.executes(commandSetTeamSpawn())));
	}
	
	private ArgumentBuilder<CommandSourceStack,?> setLivesArg() {
		return Commands.literal("set_lives")
				.then(Commands.argument("lives", IntegerArgumentType.integer(1))
				.executes(commandSetLives()));
	}
	
	private ArgumentBuilder<CommandSourceStack,?> setUseBorderArg() {
		return Commands.literal("set_use_border")
				.then(Commands.argument("use", BoolArgumentType.bool())
				.executes(commandSetUseWorldBorder()));
	}

	private ArgumentBuilder<CommandSourceStack,?> setClearOnStartArg() {
		return Commands.literal("clear_on_start")
				.then(Commands.argument("clear", BoolArgumentType.bool())
						.executes(commandSetClearOnStart()));
	}
	
	private GameSetupCom commandSetUseWorldBorder() {
		return (context, gameData) -> {
			boolean use = BoolArgumentType.getBool(context, "use");
			gameData.setUseWorldBorderDuringGame(use);
			Component message; 
			if (use) message = Component.literal(gameData.getInstanceId()+" will have a world border during game play phase.");
			else message = Component.literal(gameData.getInstanceId()+" will NOT have a world border during game play phase.");
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}

	private GameSetupCom commandSetClearOnStart() {
		return (context, gameData) -> {
			boolean clear = BoolArgumentType.getBool(context, "clear");
			gameData.setClearOnStart(clear);
			Component message;
			if (clear) message = Component.literal(gameData.getInstanceId()+" will clear player inventories on when the game starts.");
			else message = Component.literal(gameData.getInstanceId()+" will NOT clear player inventories on when the game starts.");
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
	private GameSetupCom commandSetLives() {
		return (context, gameData) -> {
			int lives = IntegerArgumentType.getInteger(context, "lives");
			gameData.setInitialLives(lives);
			Component message = Component.literal("Set "+gameData.getInstanceId()+" initial lives to "+lives);
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
	
	private GameSetupCom commandSetSize() {
		return (context, gameData) -> {
			double size = DoubleArgumentType.getDouble(context, "game_size");
			gameData.setGameBorderSize(size);
			Component message = Component.literal("Changed "+gameData.getInstanceId()+" game size to "+size);
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
	private GameSetupCom commandSetCenter() {
		return (context, gameData) -> {
			BlockPos pos = BlockPosArgument.getSpawnablePos(context, "game_center");
			gameData.setGameCenter(UtilGeometry.toVec3(pos), context.getSource().getServer());
			Component message = Component.literal("Changed "+gameData.getInstanceId()+" center pos!");
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
	private GameSetupCom commandStartGame() {
		return (context, gameData) -> {
			if (!gameData.finishSetupPhase(context.getSource().getServer())) {
				MutableComponent message = Component.literal(gameData.getInstanceId()+" is currently unable to finish setup!");
				message.append("\nUse /game setup "+gameData.getInstanceId()+" to finish settig up the game.");
				message.append("\n"+gameData.getSetupInfo());
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
