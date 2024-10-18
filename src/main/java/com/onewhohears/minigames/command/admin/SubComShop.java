package com.onewhohears.minigames.command.admin;

import java.util.Collection;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.onewhohears.minigames.command.GameComArgs;
import com.onewhohears.minigames.common.container.ShopMenu;
import com.onewhohears.minigames.data.shops.GameShop;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SubComShop {
	
	public SubComShop() {
	}
	
	public ArgumentBuilder<CommandSourceStack,?> shop() {
		return Commands.literal("shop")
			.then(openShopArg());
	}
	
	private ArgumentBuilder<CommandSourceStack,?> openShopArg() {
		return Commands.literal("open")
			.then(GameComArgs.allShopNameArgument()
			.executes(commandOpenShop(false))
				.then(Commands.argument("players", EntityArgument.players())
				.executes(commandOpenShop(true)))
			);
	}
	
	public static void openPlayerShop(ServerPlayer player, GameShop shop) {
		ShopMenu.openScreen(player, shop);
	}
	
	private Command<CommandSourceStack> commandOpenShop(boolean playerParam) {
		return (context) -> {
			String shop_name = StringArgumentType.getString(context, "shop_name");
			GameShop shop = MiniGameShopsManager.get().get(shop_name);
			if (shop == null) {
				Component message = Component.literal("There are no shops with the id "+shop_name);
				context.getSource().sendFailure(message);
				return 0;
			}
			if (!playerParam) {
				openPlayerShop(context.getSource().getPlayer(), shop);
				return 1;
			}
			Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
			for (ServerPlayer player : players) openPlayerShop(player, shop);
			Component message;
			if (players.size() > 1) 
				message = Component.literal("Opened "+players.size()+" players shop "+shop_name);
			else if (players.size() == 1) 
				message = Component.literal("Opened ").append(players.iterator().next().getDisplayName()).append(" shop "+shop_name);
			else 
				message = Component.literal("No matching players found to open shop "+shop_name);
			context.getSource().sendSuccess(message, true);
			return 1;
		};
	}
	
}
