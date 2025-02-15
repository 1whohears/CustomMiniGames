package com.onewhohears.minigames.command.admin;

import java.util.Collection;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.onewhohears.minigames.command.GameComArgs;
import com.onewhohears.minigames.data.kits.MiniGameKitsManager;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.agent.TeamAgent;

import com.onewhohears.minigames.minigame.data.BuyAttackData;
import com.onewhohears.minigames.minigame.data.KillFlagData;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.minigames.util.CommandUtil;
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
import org.apache.commons.lang3.function.TriFunction;

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
						.then(setSpawnPlayerArg())
						.then(setSpawnTeamArg())
						.then(setLivesArg())
						.then(setUseBorderArg())
						.then(setClearOnStartArg())
						.then(addKitArg()).then(removeKitArg())
						.then(addShopArg()).then(removeShopArg())
						.then(setIntParamArg("buy_time", "ticks", (context, gameData, num) -> {
							if (!(gameData instanceof BuyAttackData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							data.setBuyTime(num);
							Component message = UtilMCText.literal("Set Buy Time to "+num+" ticks!");
							context.getSource().sendSuccess(message, true);
							return 1;
						}, 0, 2000000))
						.then(setIntParamArg("attack_time", "ticks", (context, gameData, num) -> {
							if (!(gameData instanceof BuyAttackData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							data.setAttackTime(num);
							Component message = UtilMCText.literal("Set Attack Time to "+num+" ticks!");
							context.getSource().sendSuccess(message, true);
							return 1;
						}, 0, 2000000))
						.then(setIntParamArg("attack_end_time", "ticks", (context, gameData, num) -> {
							if (!(gameData instanceof BuyAttackData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							data.setAttackEndTime(num);
							Component message = UtilMCText.literal("Set Attack End Time to "+num+" ticks!");
							context.getSource().sendSuccess(message, true);
							return 1;
						}, 0, 2000000))
						.then(setIntParamArg("rounds_to_win", "rounds", (context, gameData, num) -> {
							if (!(gameData instanceof BuyAttackData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							data.setRoundsToWin(num);
							Component message = UtilMCText.literal("Set Rounds to Win to "+num+" rounds!");
							context.getSource().sendSuccess(message, true);
							return 1;
						}, 1, 1000000))
						.then(setIntParamArg("money_per_round", "money", (context, gameData, num) -> {
							gameData.setMoneyPerRound(num);
							Component message = UtilMCText.literal("Set money per round to "+num);
							context.getSource().sendSuccess(message, true);
							return 1;
						}, 1, 10000))
						.then(setIntParamArg("buy_radius", "blocks", (context, gameData, num) -> {
							if (!(gameData instanceof BuyAttackData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							data.setBuyRadius(num);
							Component message = UtilMCText.literal("Set Buy Radius to "+num);
							context.getSource().sendSuccess(message, true);
							return 1;
						}, -1, 10000))
						.then(setStringParamArg("add_attacker", "team", (context, gameData, string) -> {
							if (!(gameData instanceof KillFlagData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							if (!data.addAttacker(string)) {
								Component message = UtilMCText.literal("This team hasn't been added to the mini-game.");
								context.getSource().sendFailure(message);
								return 0;
							}
							Component message = UtilMCText.literal("Added attacker "+string);
							context.getSource().sendSuccess(message, true);
							return 1;
						}, GameComArgs.suggestAgentNames()))
						.then(setStringParamArg("add_defender", "team", (context, gameData, string) -> {
							if (!(gameData instanceof KillFlagData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							if (!data.addDefender(string)) {
								Component message = UtilMCText.literal("This team hasn't been added to the mini-game.");
								context.getSource().sendFailure(message);
								return 0;
							}
							Component message = UtilMCText.literal("Added defender "+string);
							context.getSource().sendSuccess(message, true);
							return 1;
						}, GameComArgs.suggestAgentNames()))
						.then(setStringParamArg("remove_attacker", "team", (context, gameData, string) -> {
							if (!(gameData instanceof KillFlagData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							if (!data.removeAttacker(string)) {
								Component message = UtilMCText.literal("This team hasn't been added to the mini-game.");
								context.getSource().sendFailure(message);
								return 0;
							}
							Component message = UtilMCText.literal("Removed attacker "+string);
							context.getSource().sendSuccess(message, true);
							return 1;
						}, GameComArgs.suggestAgentNames()))
						.then(setStringParamArg("remove_defender", "team", (context, gameData, string) -> {
							if (!(gameData instanceof KillFlagData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							if (!data.removeDefender(string)) {
								Component message = UtilMCText.literal("This team hasn't been added to the mini-game.");
								context.getSource().sendFailure(message);
								return 0;
							}
							Component message = UtilMCText.literal("Removed defender "+string);
							context.getSource().sendSuccess(message, true);
							return 1;
						}, GameComArgs.suggestAgentNames()))
						.then(setStringParamArg("add_attacker_shop", "shop", (context, gameData, string) -> {
							if (!(gameData instanceof KillFlagData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							if (!data.addAttackerShop(string)) {
								Component message = UtilMCText.literal("Could not add shop.");
								context.getSource().sendFailure(message);
								return 0;
							}
							Component message = UtilMCText.literal("Added Attacker Shop "+string);
							context.getSource().sendSuccess(message, true);
							return 1;
						}, CommandUtil.suggestStrings(() -> MiniGameKitsManager.get().getAllIds())))
						.then(setStringParamArg("add_defender_shop", "shop", (context, gameData, string) -> {
							if (!(gameData instanceof KillFlagData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							if (!data.addDefenderShop(string)) {
								Component message = UtilMCText.literal("Could not add shop.");
								context.getSource().sendFailure(message);
								return 0;
							}
							Component message = UtilMCText.literal("Added Defender Shop "+string);
							context.getSource().sendSuccess(message, true);
							return 1;
						}, CommandUtil.suggestStrings(() -> MiniGameKitsManager.get().getAllIds())))
						.then(setStringParamArg("remove_attacker_shop", "shop", (context, gameData, string) -> {
							if (!(gameData instanceof KillFlagData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							if (!data.removeAttackerShop(string)) {
								Component message = UtilMCText.literal("Could not remove shop.");
								context.getSource().sendFailure(message);
								return 0;
							}
							Component message = UtilMCText.literal("Removed Attacker Shop "+string);
							context.getSource().sendSuccess(message, true);
							return 1;
						}, GameComArgs.suggestEnabledShops()))
						.then(setStringParamArg("remove_defender_shop", "shop", (context, gameData, string) -> {
							if (!(gameData instanceof KillFlagData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							if (!data.removeDefenderShop(string)) {
								Component message = UtilMCText.literal("Could not remove shop.");
								context.getSource().sendFailure(message);
								return 0;
							}
							Component message = UtilMCText.literal("Removed Defender Shop "+string);
							context.getSource().sendSuccess(message, true);
							return 1;
						}, GameComArgs.suggestEnabledShops()))
						.then(setBoolParamArg("allow_respawn_during_buy", "allow", (context, gameData, value) -> {
							if (!(gameData instanceof BuyAttackData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							data.setAllowRespawnInBuyPhase(value);
							Component message;
							if (value) message = UtilMCText.literal("Players CAN respawn during the buy phase!");
							else message = UtilMCText.literal("Players can NOT respawn during the buy phase!");
							context.getSource().sendSuccess(message, true);
							return 1;
						}))
						.then(setIntParamArg("no_blocks_flag_radius", "blocks", (context, gameData, num) -> {
							if (!(gameData instanceof KillFlagData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							data.banAllBlocksRadius = num;
							Component message = UtilMCText.literal("Set No Blocks Flag Radius to "+num);
							context.getSource().sendSuccess(message, true);
							return 1;
						}, 0, 10000))
						.then(setIntParamArg("block_whitelist_flag_radius", "blocks", (context, gameData, num) -> {
							if (!(gameData instanceof KillFlagData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							data.blockWhiteListRadius = num;
							Component message = UtilMCText.literal("Set Block White List Flag Radius to "+num);
							context.getSource().sendSuccess(message, true);
							return 1;
						}, 0, 10000))
						.then(setIntParamArg("block_blacklist_flag_radius", "blocks", (context, gameData, num) -> {
							if (!(gameData instanceof KillFlagData data)) {
								Component message = UtilMCText.literal("This game doesn't use this parameter.");
								context.getSource().sendFailure(message);
								return 0;
							}
							data.blockBlackListRadius = num;
							Component message = UtilMCText.literal("Set Block Black List Flag Radius to "+num);
							context.getSource().sendSuccess(message, true);
							return 1;
						}, 0, 10000))
						.then(setBoolParamArg("force_non_member_spectator", "force", (context, gameData, value) -> {
							gameData.forceNonMemberSpectator = value;
							Component message;
							if (value) message = UtilMCText.literal("Players will be put in spectator if they have not been added to the game!");
							else message = UtilMCText.literal("Players will NOT be put in spectator if they have not been added to the game!");
							context.getSource().sendSuccess(message, true);
							return 1;
						}))
			);
	}

	private ArgumentBuilder<CommandSourceStack,?> setBoolParamArg(String argName, String valueName,
																 TriFunction<CommandContext<CommandSourceStack>,
																		 MiniGameData, Boolean, Integer> consumer) {
		return Commands.literal(argName)
				.then(Commands.argument(valueName, BoolArgumentType.bool())
						.executes(commandBool(valueName, consumer)));
	}

	private GameSetupCom commandBool(String valueName, TriFunction<
			CommandContext<CommandSourceStack>, MiniGameData, Boolean, Integer> consumer) {
		return (context, gameData) -> {
			boolean value = BoolArgumentType.getBool(context, valueName);
			return consumer.apply(context, gameData, value);
		};
	}

	private ArgumentBuilder<CommandSourceStack,?> setStringParamArg(String argName, String valueName,
																	TriFunction<CommandContext<CommandSourceStack>,
																		 MiniGameData, String, Integer> consumer,
																	SuggestionProvider<CommandSourceStack> suggests) {
		return Commands.literal(argName)
				.then(Commands.argument(valueName, StringArgumentType.string())
						.suggests(suggests).executes(commandString(valueName, consumer)));
	}

	private GameSetupCom commandString(String valueName, TriFunction<
			CommandContext<CommandSourceStack>, MiniGameData, String, Integer> consumer) {
		return (context, gameData) -> {
			String string = StringArgumentType.getString(context, valueName);
			return consumer.apply(context, gameData, string);
		};
	}

	private ArgumentBuilder<CommandSourceStack,?> setIntParamArg(String argName, String valueName,
																 TriFunction<CommandContext<CommandSourceStack>,
																		 MiniGameData, Integer, Integer> consumer,
																 int min, int max) {
		return Commands.literal(argName)
				.then(Commands.argument(valueName, IntegerArgumentType.integer(min, max))
						.executes(commandInteger(valueName, consumer)));
	}

	private GameSetupCom commandInteger(String valueName, TriFunction<
			CommandContext<CommandSourceStack>, MiniGameData, Integer, Integer> consumer) {
		return (context, gameData) -> {
			int num = IntegerArgumentType.getInteger(context, valueName);
			return consumer.apply(context, gameData, num);
		};
	}

	private ArgumentBuilder<CommandSourceStack,?> addKitArg() {
		return Commands.literal("add_kit")
				.then(GameComArgs.allKitNameArgument()
						.executes(commandAddKit()));
	}

	private ArgumentBuilder<CommandSourceStack,?> removeKitArg() {
		return Commands.literal("remove_kit")
				.then(GameComArgs.enabledKitNameArgument()
						.executes(commandRemoveKit()));
	}

	private ArgumentBuilder<CommandSourceStack,?> addShopArg() {
		return Commands.literal("add_shop")
				.then(GameComArgs.allKitNameArgument()
						.executes(commandAddShop()));
	}

	private ArgumentBuilder<CommandSourceStack,?> removeShopArg() {
		return Commands.literal("remove_shop")
				.then(GameComArgs.enabledKitNameArgument()
						.executes(commandRemoveShop()));
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
			String reason  = gameData.getStartFailedReason(context.getSource().getServer());
			if (reason != null) {
				MutableComponent message = Component.literal(gameData.getInstanceId()+" is currently unable to finish setup!");
				message.append("\nUse /game setup "+gameData.getInstanceId()+" to finish setting up the game.");
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

	private GameSetupCom commandAddKit() {
		return (context, gameData) -> {
			String kit_name = StringArgumentType.getString(context, "kit_name");
			if (gameData.hasKit(kit_name)) {
				Component message = Component.literal(gameData.getInstanceId()+" already has kit "+kit_name+" enabled!");
				context.getSource().sendSuccess(message, true);
				return 1;
			}
			if (!MiniGameKitsManager.get().has(kit_name)) {
				Component message = Component.literal("Kit "+kit_name+" does not exist!");
				context.getSource().sendFailure(message);
				return 0;
			}
			gameData.addKits(kit_name);
			MutableComponent message = Component.literal("Added kit "+kit_name+" to "+gameData.getInstanceId()+"!");
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}

	private GameSetupCom commandRemoveKit() {
		return (context, gameData) -> {
			String kit_name = StringArgumentType.getString(context, "kit_name");
			if (!gameData.hasKit(kit_name)) {
				Component message = Component.literal(gameData.getInstanceId()+" doesn't have kit "+kit_name+"!");
				context.getSource().sendSuccess(message, true);
				return 1;
			}
			gameData.removeKit(kit_name);
			MutableComponent message = Component.literal("Removed kit "+kit_name+" from "+gameData.getInstanceId()+"!");
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}

	private GameSetupCom commandAddShop() {
		return (context, gameData) -> {
			String shop_name = StringArgumentType.getString(context, "shop_name");
			if (gameData.hasShop(shop_name)) {
				Component message = Component.literal(gameData.getInstanceId()+" already has shop "+shop_name+" enabled!");
				context.getSource().sendSuccess(message, true);
				return 1;
			}
			if (!MiniGameShopsManager.get().has(shop_name)) {
				Component message = Component.literal("Shop "+shop_name+" does not exist!");
				context.getSource().sendFailure(message);
				return 0;
			}
			gameData.addShops(shop_name);
			MutableComponent message = Component.literal("Added shop "+shop_name+" to "+gameData.getInstanceId()+"!");
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}

	private GameSetupCom commandRemoveShop() {
		return (context, gameData) -> {
			String shop_name = StringArgumentType.getString(context, "shop_name");
			if (!gameData.hasShop(shop_name)) {
				Component message = Component.literal(gameData.getInstanceId()+" doesn't have shop "+shop_name+"!");
				context.getSource().sendSuccess(message, true);
				return 1;
			}
			gameData.removeShops(shop_name);
			MutableComponent message = Component.literal("Removed shop "+shop_name+" from "+gameData.getInstanceId()+"!");
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
}
