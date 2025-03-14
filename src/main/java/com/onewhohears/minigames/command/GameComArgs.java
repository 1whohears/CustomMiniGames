package com.onewhohears.minigames.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.onewhohears.minigames.data.kits.MiniGameKitsManager;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.param.MiniGameParamTypes;
import com.onewhohears.minigames.minigame.param.SetParamType;
import com.onewhohears.minigames.util.CommandUtil;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.Set;

public class GameComArgs {
	
	public static ArgumentBuilder<CommandSourceStack,?> runningGameIdArgument() {
		return Commands.argument("instance_id", StringArgumentType.word())
				.suggests(CommandUtil.suggestStrings(() -> MiniGameManager.get().getRunningGameIds()));
	}
	
	public static ArgumentBuilder<CommandSourceStack,?> gameTypeIdArgument() {
		return Commands.argument("game_type", StringArgumentType.word())
				.suggests(CommandUtil.suggestStrings(MiniGameManager.getNewGameTypeIds()));
	}
	
	public static ArgumentBuilder<CommandSourceStack,?> allKitNameArgument() {
		return Commands.argument("kit_name", StringArgumentType.word())
				.suggests(CommandUtil.suggestStrings(() -> MiniGameKitsManager.get().getAllIds()));
	}
	
	public static ArgumentBuilder<CommandSourceStack,?> allShopNameArgument() {
		return Commands.argument("shop_name", StringArgumentType.word())
				.suggests(CommandUtil.suggestStrings(() -> MiniGameShopsManager.get().getAllIds()));
	}
	
	public static ArgumentBuilder<CommandSourceStack,?> enabledKitNameArgument() {
		return Commands.argument("kit_name", StringArgumentType.word())
				.suggests(suggestEnabledKits());
	}
	
	public static ArgumentBuilder<CommandSourceStack,?> enabledShopNameArgument() {
		return Commands.argument("shop_name", StringArgumentType.word())
				.suggests(suggestEnabledShops());
	}
	
	public static PlayerAgentSuggestion suggestEnabledKits() {
		return (context, builder, agents) -> {
			for (PlayerAgent agent : agents)
				CommandUtil.suggestStringToBuilder(builder, agent.getAvailableKits());
			return builder.buildFuture();
		};
	}
	
	public static PlayerAgentSuggestion suggestEnabledShops() {
		return (context, builder, agents) -> {
			for (PlayerAgent agent : agents)
				CommandUtil.suggestStringToBuilder(builder, agent.getAvailableShops());
			return builder.buildFuture();
		};
	}

	public static PlayerAgentSuggestion suggestAgentNames() {
		return (context, builder, agents) -> {
			for (PlayerAgent agent : agents)
				CommandUtil.suggestStringToBuilder(builder, agent.getGameData().getAllAgentIds());
			return builder.buildFuture();
		};
	}

	public static PlayerAgentSuggestion suggestPlayerAgentNames() {
		return (context, builder, agents) -> {
			for (PlayerAgent agent : agents)
				CommandUtil.suggestStringToBuilder(builder, agent.getGameData().getAllPlayerAgentNames());
			return builder.buildFuture();
		};
	}

	public static PlayerAgentSuggestion suggestTeamAgentNames() {
		return (context, builder, agents) -> {
			for (PlayerAgent agent : agents)
				CommandUtil.suggestStringToBuilder(builder, agent.getGameData().getAllTeamAgentNames());
			return builder.buildFuture();
		};
	}

	public static PlayerAgentSuggestion suggestHandleableEvents() {
		return suggestFromSet(MiniGameParamTypes.EVENTS);
	}

	public static PlayerAgentSuggestion suggestNothing() {
		return (context, builder, agents) -> builder.buildFuture();
	}

	public static PlayerAgentSuggestion suggestHandleablePoiTypes() {
		return suggestFromSet(MiniGameParamTypes.POI_TYPES);
	}

	public static PlayerAgentSuggestion suggestAddedPois() {
		return (context, builder, agents) -> {
			for (PlayerAgent agent : agents)
				CommandUtil.suggestStringToBuilder(builder, agent.getGameData().getPOIInstanceIds());
			return builder.buildFuture();
		};
	}

	public static <C extends Set<E>, E> PlayerAgentSuggestion suggestFromSet(SetParamType<C,E> type) {
		return (context, builder, agents) -> {
			for (PlayerAgent agent : agents)
				CommandUtil.suggestStringToBuilder(builder, agent.getGameData().getStringsFromType(type));
			return builder.buildFuture();
		};
	}
}
