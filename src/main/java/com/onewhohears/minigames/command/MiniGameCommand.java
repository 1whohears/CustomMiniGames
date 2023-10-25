package com.onewhohears.minigames.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.agent.TeamAgent;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.minigames.util.UtilConvert;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;

public class MiniGameCommand {
	
	public MiniGameCommand(CommandDispatcher<CommandSourceStack> d) {
		d.register(Commands.literal("minigame").requires((stack) -> { return stack.hasPermission(2);})
			.then(createNew())
			.then(setup())
			.then(reset())
			.then(remove())
			.then(listRunning())
			.then(info())
		);
	}
	
	private ArgumentBuilder<CommandSourceStack,?> info() {
		return Commands.literal("info")
			.then(Commands.argument("instance_id", StringArgumentType.word())
			.suggests(suggestStrings(() -> MiniGameManager.get().getRunningeGameIds()))
				.then(Commands.literal("list_players").executes(commandPlayerList()))
			);
	}
	
	private GameDataCommand commandPlayerList() {
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
	
	private ArgumentBuilder<CommandSourceStack,?> setup() {
		return Commands.literal("setup")
			.then(Commands.argument("instance_id", StringArgumentType.word())
			.suggests(suggestStrings(() -> MiniGameManager.get().getRunningeGameIds()))
				.then(Commands.literal("start").executes(commandStartGame()))
				.then(Commands.literal("add_team")
					.then(Commands.argument("team", TeamArgument.team())
					.executes(commandAddTeam())))
				.then(Commands.literal("remove_team")
					.then(Commands.argument("team", TeamArgument.team())
					.executes(commandRemoveTeam())))
				.then(Commands.literal("add_player")
					.then(Commands.argument("player", EntityArgument.players())
					.executes(commandAddPlayers())))
				.then(Commands.literal("remove_player")
					.then(Commands.argument("player", EntityArgument.players())
					.executes(commandRemovePlayers())))
				.then(Commands.literal("set_center")
					.then(Commands.argument("game_center", BlockPosArgument.blockPos())
					.executes(commandSetCenter())))
				.then(Commands.literal("set_size")
					.then(Commands.argument("game_size", DoubleArgumentType.doubleArg(-5.9999968E7D, 5.9999968E7D))
					.executes(commandSetSize())))
				.then(Commands.literal("set_spawn")
					.then(Commands.argument("player", EntityArgument.players())
						.then(Commands.argument("spawn_pos", BlockPosArgument.blockPos())
							.executes(commandSetPlayerSpawn())))
					.then(Commands.argument("team", TeamArgument.team())
						.then(Commands.argument("spawn_pos", BlockPosArgument.blockPos())
							.executes(commandSetTeamSpawn()))))
				.then(Commands.literal("set_lives")
					.then(Commands.argument("lives", IntegerArgumentType.integer(1))
					.executes(commandSetLives())))
				.then(Commands.literal("set_use_border")
					.then(Commands.argument("use", BoolArgumentType.bool())
					.executes(commandSetUseWorldBorder())))
			);
	}
	
	private GameSetupCommand commandSetUseWorldBorder() {
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
	
	private GameSetupCommand commandSetLives() {
		return (context, gameData) -> {
			int lives = IntegerArgumentType.getInteger(context, "lives");
			gameData.setInitialLives(lives);
			Component message = Component.literal("Set "+gameData.getInstanceId()+" initial lives to "+lives);
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
	private GameSetupCommand commandSetPlayerSpawn() {
		return (context, gameData) -> {
			Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "player");
			if (players.size() == 0) {
				Component message = Component.literal("No player spawnpoints were changed");
				context.getSource().sendSuccess(message, true);
				return 1;
			}
			BlockPos pos = BlockPosArgument.getSpawnablePos(context, "spawn_pos");
			for (ServerPlayer player : players) {
				PlayerAgent<?> agent = gameData.getPlayerAgentByUUID(player.getStringUUID());
				if (agent == null) {
					Component message = Component.literal("The player ").append(player.getDisplayName())
							.append(" is not in the game "+gameData.getInstanceId());
					context.getSource().sendFailure(message);
					continue;
				}
				agent.setRespawnPoint(UtilConvert.toVec3(pos));
				Component message = Component.literal("Set ").append(player.getDisplayName())
						.append(" spawn point to "+pos.toShortString());
				context.getSource().sendSuccess(message, true);
			}
			return 1;
		};
	}
	
	private GameSetupCommand commandSetTeamSpawn() {
		return (context, gameData) -> {
			PlayerTeam team = TeamArgument.getTeam(context, "team");
			BlockPos pos = BlockPosArgument.getSpawnablePos(context, "spawn_pos");
			TeamAgent<?> agent = gameData.getTeamAgentByName(team.getName());
			if (agent == null) {
				Component message = Component.literal("The team ").append(team.getDisplayName())
						.append(" is not in the game "+gameData.getInstanceId());
				context.getSource().sendFailure(message);
				return 0;
			}
			agent.setRespawnPoint(UtilConvert.toVec3(pos));
			Component message = Component.literal("Set ").append(team.getDisplayName())
					.append(" spawn point to "+pos.toShortString());
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
	private GameSetupCommand commandSetSize() {
		return (context, gameData) -> {
			double size = DoubleArgumentType.getDouble(context, "game_size");
			gameData.setGameBorderSize(size);
			Component message = Component.literal("Changed "+gameData.getInstanceId()+" game size to "+size);
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
	private GameSetupCommand commandSetCenter() {
		return (context, gameData) -> {
			BlockPos pos = BlockPosArgument.getSpawnablePos(context, "game_center");
			gameData.setGameCenter(UtilConvert.toVec3(pos), context.getSource().getServer());
			Component message = Component.literal("Changed "+gameData.getInstanceId()+" center pos!");
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
	private GameSetupCommand commandStartGame() {
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
	
	private GameSetupCommand commandRemovePlayers() {
		return (context, gameData) -> {
			Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "player");
			if (players.size() == 0) {
				context.getSource().sendSuccess(Component.literal("No players could be removed."), true);
				return 1;
			}
			for (ServerPlayer player : players) {
				if (gameData.removeAgentById(player.getStringUUID())) {
					Component message = Component.literal("Failed to remove ").append(player.getDisplayName()).append(" from "+gameData.getInstanceId()+"!");
					context.getSource().sendFailure(message);
				} else {
					Component message = Component.literal("Removed player ").append(player.getDisplayName()).append(" from "+gameData.getInstanceId()+"!");
					context.getSource().sendSuccess(message, true);
				}
			}
			return 1;
		};
	}
	
	private GameSetupCommand commandAddPlayers() {
		return (context, gameData) -> {
			if (!gameData.canAddIndividualPlayers()) {
				Component message = Component.literal("The game instance "+gameData.getInstanceId()+" does not allow individual players!");
				context.getSource().sendFailure(message);
				return 0;
			}
			Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "player");
			if (players.size() == 0) {
				context.getSource().sendSuccess(Component.literal("No players could be added."), true);
				return 1;
			}
			for (ServerPlayer player : players) {
				if (gameData.getAddIndividualPlayer(player) == null) {
					Component message = Component.literal("Failed to add ").append(player.getDisplayName()).append(" to "+gameData.getInstanceId()+"!");
					context.getSource().sendFailure(message);
				} else {
					Component message = Component.literal("Added player ").append(player.getDisplayName()).append(" to "+gameData.getInstanceId()+"!");
					context.getSource().sendSuccess(message, true);
				}
			}
			return 1;
		};
	}
	
	private GameSetupCommand commandRemoveTeam() {
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
	
	private GameSetupCommand commandAddTeam() {
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
	
	private ArgumentBuilder<CommandSourceStack,?> createNew() {
		return Commands.literal("create_new")
			.then(Commands.argument("game_type", StringArgumentType.word())
			.suggests(suggestStrings(MiniGameManager.getNewGameTypeIds()))
				.then(Commands.argument("instance_id", StringArgumentType.word())
				.executes(commandCreateNew()))
			);
	}
	
	private Command<CommandSourceStack> commandCreateNew() {
		return (context) -> {
			String gameTypeId = StringArgumentType.getString(context, "game_type");
			if (!MiniGameManager.hasGameType(gameTypeId)) {
				Component message = Component.literal("The Game Type "+gameTypeId+" does not exist!");
				context.getSource().sendFailure(message);
				return 0;
			}
			String gameInstanceId = StringArgumentType.getString(context, "instance_id");
			if (MiniGameManager.get().isGameRunning(gameInstanceId)) {
				Component message = Component.literal(gameInstanceId+" already exists! You may want to reset or remove it.");
				context.getSource().sendFailure(message);
				return 0;
			}
			MiniGameData gameData = MiniGameManager.get().startNewGame(gameTypeId, gameInstanceId);
			if (gameData == null) {
				Component message = Component.literal("Unable to start new game "+gameInstanceId+" of type "+gameTypeId);
				context.getSource().sendFailure(message);
				return 0;
			}
			gameData.setGameCenter(context.getSource().getPosition());
			MutableComponent message = Component.literal("Started new game of type "+gameTypeId+" called "+gameInstanceId+".");
			message.append("\nUse /game setup "+gameInstanceId+" to configure the game.");
			message.append("\n"+gameData.getSetupInfo());
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
	private ArgumentBuilder<CommandSourceStack,?> reset() {
		return Commands.literal("reset")
			.then(Commands.argument("instance_id", StringArgumentType.word())
			.suggests(suggestStrings(() -> MiniGameManager.get().getRunningeGameIds()))
				.then(Commands.literal("confirm_reset")
				.executes(commandReset()))
			);
	}
	
	private GameDataCommand commandReset() {
		return (context, gameData) -> {
			gameData.reset(context.getSource().getServer());
			Component message = Component.literal(gameData.getInstanceId()+" was reset!");
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
	private ArgumentBuilder<CommandSourceStack,?> remove() {
		return Commands.literal("remove")
			.then(Commands.argument("instance_id", StringArgumentType.word())
			.suggests(suggestStrings(() -> MiniGameManager.get().getRunningeGameIds()))
				.then(Commands.literal("confirm_remove")
				.executes(commandRemove()))
			);
	}
	
	private GameDataCommand commandRemove() {
		return (context, gameData) -> {
			gameData.reset(context.getSource().getServer());
			MiniGameManager.get().removeGame(gameData.getInstanceId());
			Component message = Component.literal(gameData.getInstanceId()+" was removed!");
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
	
	public static SuggestionProvider<CommandSourceStack> suggestStrings(String[] strings) {
		return (context, builder) -> {
			return SharedSuggestionProvider.suggest(strings, builder);
		};
	}
	
	public static SuggestionProvider<CommandSourceStack> suggestStrings(Supplier<String[]> strings) {
		return (context, builder) -> {
			return SharedSuggestionProvider.suggest(strings.get(), builder);
		};
	}
	
	public interface GameDataCommand extends Command<CommandSourceStack> {
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
	
	public interface GameSetupCommand extends GameDataCommand {
		default int runGameData(CommandContext<CommandSourceStack> context, MiniGameData gameData) throws CommandSyntaxException {
			if (!gameData.isSetupPhase()) {
				Component message = Component.literal("The game instance "+gameData.getInstanceId()+" is not in the setup phase! You must reset (Dangerous!)");
				context.getSource().sendFailure(message);
				return 0;
			}
			return runSetup(context, gameData);
		}
		int runSetup(CommandContext<CommandSourceStack> context, MiniGameData gameData) throws CommandSyntaxException;
	}
	
}
