package com.onewhohears.minigames.command.admin;

import java.util.Collection;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.onewhohears.minigames.data.kits.GameKit;
import com.onewhohears.minigames.data.kits.MiniGameKitsManager;
import com.onewhohears.minigames.util.CommandUtil;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SubComKit {
	
	public static ArgumentBuilder<CommandSourceStack,?> kitNameArgument() {
		return Commands.argument("kit_name", StringArgumentType.word())
				.suggests(CommandUtil.suggestStrings(() -> MiniGameKitsManager.get().getKitNames()));
	}
	
	public SubComKit() {
	}
	
	public ArgumentBuilder<CommandSourceStack,?> kit() {
		return Commands.literal("kit")
			.then(giveKit());
	}
	
	private ArgumentBuilder<CommandSourceStack,?> giveKit() {
		return Commands.literal("give")
			.then(kitNameArgument()
				.then(Commands.argument("players", EntityArgument.players())
				.executes(commandGiveKit(false, false, false))
					.then(Commands.literal("give_all")
					.executes(commandGiveKit(false, false, false)))
					.then(Commands.literal("refill")
					.executes(commandGiveKit(false, false, true)))
					.then(Commands.literal("clear_other")
					.executes(commandGiveKit(false, true, false)))
					.then(Commands.literal("clear_all")
					.executes(commandGiveKit(true, false, false)))
				)	
			);
	}
	
	private Command<CommandSourceStack> commandGiveKit(boolean clearAll, boolean clearOther, boolean refill) {
		return (context) -> {
			String kit_name = StringArgumentType.getString(context, "kit_name");
			GameKit kit = MiniGameKitsManager.get().getKit(kit_name);
			if (kit == null) {
				Component message = Component.literal("There are no kits with the id "+kit_name);
				context.getSource().sendFailure(message);
				return 0;
			}
			Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
			for (ServerPlayer player : players) {
				if (refill) kit.giveItemsRefill(player);
				else if (clearOther) kit.giveItemsClearOther(player);
				else if (clearAll) kit.giveItemsClearAll(player);
				else kit.giveItems(player);
				Component message = Component.literal("You recieved kit "+kit_name);
				player.displayClientMessage(message, false);
			}
			Component message;
			if (players.size() > 1) 
				message = Component.literal("Gave "+players.size()+" players the kit "+kit_name);
			else if (players.size() == 1) 
				message = Component.literal("Gave "+players.iterator().next().getDisplayName()+" the kit "+kit_name);
			else 
				message = Component.literal("No matching players found to give kit "+kit_name);
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
}
