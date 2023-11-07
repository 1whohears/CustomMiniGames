package com.onewhohears.minigames.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.onewhohears.minigames.data.kits.MiniGameKitsManager;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.minigames.minigame.MiniGameManager;
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
	
	public static ArgumentBuilder<CommandSourceStack,?> kitNameArgument() {
		return Commands.argument("kit_name", StringArgumentType.word())
				.suggests(CommandUtil.suggestStrings(() -> MiniGameKitsManager.get().getKitNames()));
	}
	
	public static ArgumentBuilder<CommandSourceStack,?> shopNameArgument() {
		return Commands.argument("shop_name", StringArgumentType.word())
				.suggests(CommandUtil.suggestStrings(() -> MiniGameShopsManager.get().getShopNames()));
	}
	
}
