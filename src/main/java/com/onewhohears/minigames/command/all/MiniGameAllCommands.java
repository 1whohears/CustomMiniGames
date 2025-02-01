package com.onewhohears.minigames.command.all;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.onewhohears.minigames.command.GameComArgs;
import com.onewhohears.minigames.command.admin.SubComShop;
import com.onewhohears.minigames.data.shops.GameShop;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class MiniGameAllCommands {
	
	public MiniGameAllCommands(CommandDispatcher<CommandSourceStack> d) {
		d.register(Commands.literal("shop").requires((stack) -> stack.hasPermission(0))
			.then(GameComArgs.enabledShopNameArgument()
			.executes(openShopCommand()))
		);
		d.register(Commands.literal("kit").requires((stack) -> stack.hasPermission(0))
			.then(GameComArgs.enabledKitNameArgument()
			.executes(selectKitCommand()))
		);
	}
	
	private PlayerAgentsCommand openShopCommand() {
		return (context, agents) -> {
			String shop_name = StringArgumentType.getString(context, "shop_name");
			GameShop shop = MiniGameShopsManager.get().get(shop_name);
			if (shop == null) {
				Component message = Component.literal("There are no shops with the id "+shop_name);
				context.getSource().sendFailure(message);
				return 0;
			}
			for (PlayerAgent agent : agents) {
				if (!agent.canOpenShop(shop_name)) continue;
				SubComShop.openPlayerShop(agent.getPlayer(context.getSource().getServer()), shop);
				Component message = Component.literal("Opened shop "+shop_name);
				context.getSource().sendSuccess(message, false);
				return 1;
			}
			Component message = Component.literal("You can't currently open the shop "+shop_name);
			context.getSource().sendFailure(message);
			return 0;
		};
	}
	
	private PlayerAgentsCommand selectKitCommand() {
		return (context, agents) -> {
			String kit_name = StringArgumentType.getString(context, "kit_name");
			for (PlayerAgent agent : agents) {
				if (!agent.canUseKit(kit_name)) continue;
				agent.setSelectedKit(kit_name);
				Component message = Component.literal("Changed kit to "+kit_name);
				context.getSource().sendSuccess(message, false);
				return 1;
			}
			Component message = Component.literal("You can't currently change your kit to "+kit_name);
			context.getSource().sendFailure(message);
			return 0;
		};
	}
	
}
