package com.onewhohears.minigames.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.onewhohears.minigames.data.kits.MiniGameKitsManager;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.util.CommandUtil;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class GameComArgs {
	
	public static ArgumentBuilder<CommandSourceStack,?> runningGameIdArgument() {
		return Commands.argument("instance_id", StringArgumentType.word())
				.suggests(CommandUtil.suggestStrings(() -> MiniGameManager.get().getRunningeGameIds()));
	}
	
	public static ArgumentBuilder<CommandSourceStack,?> gameTypeIdArgument() {
		return Commands.argument("game_type", StringArgumentType.word())
				.suggests(CommandUtil.suggestStrings(MiniGameManager.getNewGameTypeIds()));
	}
	
	public static ArgumentBuilder<CommandSourceStack,?> allKitNameArgument() {
		return Commands.argument("kit_name", StringArgumentType.word())
				.suggests(CommandUtil.suggestStrings(() -> MiniGameKitsManager.get().getKitNames()));
	}
	
	public static ArgumentBuilder<CommandSourceStack,?> allShopNameArgument() {
		return Commands.argument("shop_name", StringArgumentType.word())
				.suggests(CommandUtil.suggestStrings(() -> MiniGameShopsManager.get().getShopNames()));
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
			for (PlayerAgent<?> agent : agents) 
				CommandUtil.suggestStringToBuilder(builder, agent.getGameData().getEnabledKitIds());
			return builder.buildFuture();
		};
	}
	
	public static PlayerAgentSuggestion suggestEnabledShops() {
		return (context, builder, agents) -> {
			for (PlayerAgent<?> agent : agents) 
				CommandUtil.suggestStringToBuilder(builder, agent.getGameData().getEnabledShopIds());
			return builder.buildFuture();
		};
	}
	
}
