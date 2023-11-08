package com.onewhohears.minigames.command.admin;

import java.util.Collection;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.onewhohears.minigames.command.GameComArgs;
import com.onewhohears.minigames.data.kits.GameKit;
import com.onewhohears.minigames.data.kits.MiniGameKitsManager;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SubComKit {
	
	public SubComKit() {
	}
	
	public ArgumentBuilder<CommandSourceStack,?> kit() {
		return Commands.literal("kit")
			.then(giveKitArg());
	}
	
	private ArgumentBuilder<CommandSourceStack,?> giveKitArg() {
		return Commands.literal("give")
			.then(GameComArgs.kitNameArgument().executes(commandGiveKitAll(false))
				.then(Commands.argument("players", EntityArgument.players()).executes(commandGiveKitAll(true))
					.then(Commands.literal("give_all").executes(commandGiveKitAll(true)))
					.then(Commands.literal("refill").executes(commandGiveKitRefill(true)))
					.then(Commands.literal("clear_other").executes(commandGiveKitClearOther(true)))
					.then(Commands.literal("clear_all").executes(commandGiveKitClearAll(true)))
				)	
			);
	}
	
	private void givePlayerKit(ServerPlayer player, GameKit kit, boolean clearAll, boolean clearOther, boolean refill) {
		if (refill) kit.giveItemsRefill(player);
		else if (clearOther) kit.giveItemsClearOther(player);
		else if (clearAll) kit.giveItemsClearAll(player);
		else kit.giveItems(player);
		Component message = Component.literal("You recieved kit "+kit.getId());
		player.displayClientMessage(message, false);
	}
	
	private Command<CommandSourceStack> commandGiveKit(boolean clearAll, boolean clearOther, boolean refill, boolean playerParam) {
		return (context) -> {
			String kit_name = StringArgumentType.getString(context, "kit_name");
			GameKit kit = MiniGameKitsManager.get().getKit(kit_name);
			if (kit == null) {
				Component message = Component.literal("There are no kits with the id "+kit_name);
				context.getSource().sendFailure(message);
				return 0;
			}
			if (!playerParam) {
				givePlayerKit(context.getSource().getPlayer(), kit, clearAll, clearOther, refill);
				return 1;
			}
			Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
			for (ServerPlayer player : players) givePlayerKit(player, kit, clearAll, clearOther, refill);
			Component message;
			if (players.size() > 1) 
				message = Component.literal("Gave "+players.size()+" players the kit "+kit_name);
			else if (players.size() == 1) 
				message = Component.literal("Gave ").append(players.iterator().next().getDisplayName()).append(" the kit "+kit_name);
			else 
				message = Component.literal("No matching players found to give kit "+kit_name);
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	private Command<CommandSourceStack> commandGiveKitAll(boolean playerParam) {
		return commandGiveKit(false, false, false, playerParam);
	}
	private Command<CommandSourceStack> commandGiveKitRefill(boolean playerParam) {
		return commandGiveKit(false, false, true, playerParam);
	}
	private Command<CommandSourceStack> commandGiveKitClearOther(boolean playerParam) {
		return commandGiveKit(false, true, false, playerParam);
	}
	private Command<CommandSourceStack> commandGiveKitClearAll(boolean playerParam) {
		return commandGiveKit(true, false, false, playerParam);
	}
	
}
