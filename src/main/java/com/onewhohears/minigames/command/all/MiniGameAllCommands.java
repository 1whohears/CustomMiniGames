package com.onewhohears.minigames.command.all;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.onewhohears.minigames.command.GameComArgs;
import com.onewhohears.minigames.command.admin.SubComShop;
import com.onewhohears.minigames.common.network.PacketHandler;
import com.onewhohears.minigames.common.network.toclient.ToClientOpenKitGUI;
import com.onewhohears.minigames.common.network.toclient.ToClientOpenShopGUI;
import com.onewhohears.minigames.common.network.toclient.ToClientGameJoinGUI;
import com.onewhohears.minigames.data.shops.GameShop;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;

import com.onewhohears.minigames.minigame.data.MiniGameData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiniGameAllCommands {
	
	public MiniGameAllCommands(CommandDispatcher<CommandSourceStack> d) {
		d.register(Commands.literal("shop").requires((stack) -> stack.hasPermission(0))
				.executes(openShopGUICommand())
				.then(GameComArgs.enabledShopNameArgument()
						.executes(openShopCommand()))
		);
		d.register(Commands.literal("kit").requires((stack) -> stack.hasPermission(0))
				.executes(openKitGUICommand())
				.then(GameComArgs.enabledKitNameArgument()
						.executes(selectKitCommand()))
		);
		d.register(Commands.literal("joingame").requires((stack) -> stack.hasPermission(0))
				.executes(openJoinGameGUICommand())
		);
	}

	private Command<CommandSourceStack> openJoinGameGUICommand() {
		return context -> {
			ServerPlayer player = context.getSource().getPlayer();
			if (player == null) {
				Component message = Component.literal("This command must be used by a player!");
				context.getSource().sendFailure(message);
				return 0;
			}
			List<String> joinAbleGames = new ArrayList<>();
			Map<String, String[]> teamMap = new HashMap<>();
			String[] ids = MiniGameManager.get().getRunningGameIds();
            for (String id : ids) {
                MiniGameData data = MiniGameManager.get().getRunningGame(id);
                if (data == null) continue;
                if (data.canPlayerJoinViaGUI()) joinAbleGames.add(id);
				else continue;
				if (data.canPlayersPickTeams()) teamMap.put(id, data.getTeamIds());
            }
			PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> context.getSource().getPlayer()),
					new ToClientGameJoinGUI(joinAbleGames.toArray(new String[0]), teamMap));
			return 1;
		};
	}

	private PlayerAgentsCommand openShopGUICommand() {
		return (context, agents) -> {
			List<String> shops = new ArrayList<>();
			for (PlayerAgent agent : agents) {
				String[] a = agent.getAvailableShops();
				for (String s : a) if (agent.canOpenShop(s)) shops.add(s);
			}
			if (shops.isEmpty()) {
				Component message = Component.literal("You are currently not allowed to open shops!");
				context.getSource().sendFailure(message);
				return 0;
			}
			PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> context.getSource().getPlayer()),
					new ToClientOpenShopGUI(shops.toArray(new String[0])));
			return 1;
		};
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

	private PlayerAgentsCommand openKitGUICommand() {
		return (context, agents) -> {
			String selected = "";
			List<String> kits = new ArrayList<>();
			for (PlayerAgent agent : agents) {
				selected = agent.getSelectedKit();
				String[] a = agent.getAvailableKits();
				for (String s : a) if (agent.canUseKit(s)) kits.add(s);
			}
			if (kits.isEmpty()) {
				Component message = Component.literal("You aren't in an active game");
				context.getSource().sendFailure(message);
				return 0;
			}
			PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> context.getSource().getPlayer()),
					new ToClientOpenKitGUI(selected, kits.toArray(new String[0])));
			return 1;
		};
	}
	
}
