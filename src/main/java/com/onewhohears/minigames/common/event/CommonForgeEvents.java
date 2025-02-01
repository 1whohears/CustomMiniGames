package com.onewhohears.minigames.common.event;

import com.onewhohears.onewholibs.common.event.GetJsonPresetListenersEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.onewhohears.minigames.MiniGamesMod;
import com.onewhohears.minigames.data.kits.MiniGameKitsManager;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = MiniGamesMod.MODID, bus = Bus.FORGE)
public class CommonForgeEvents {
	
	private static final Logger LOGGER = LogUtils.getLogger();
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void livingDeathEvent(LivingDeathEvent event) {
		if (event.getEntity().getLevel().isClientSide()) return;
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		for (PlayerAgent agent : MiniGameManager.get().getActiveGamePlayerAgents(player))
			if (agent.shouldRunOnDeath())
				agent.onDeath(player.getServer(), event.getSource());
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity().getLevel().isClientSide() || event.isEndConquered()) return;
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		for (PlayerAgent agent : MiniGameManager.get().getActiveGamePlayerAgents(player))
			if (agent.shouldRunOnRespawn())
				agent.onRespawn(player.getServer());
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity().getLevel().isClientSide()) return;
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		for (PlayerAgent agent : MiniGameManager.get().getActiveGamePlayerAgents(player))
			if (agent.shouldRunOnRespawn())
				agent.onLogIn(player.getServer());
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity().getLevel().isClientSide()) return;
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		for (PlayerAgent agent : MiniGameManager.get().getActiveGamePlayerAgents(player))
			if (agent.shouldRunOnRespawn())
				agent.onLogOut(player.getServer());
	}
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void serverTickEvent(TickEvent.ServerTickEvent event) {
		if (event.phase != Phase.END) return;
		MiniGameManager.get().serverTick(event.getServer());
	}
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void serverStartedEvent(ServerStartedEvent event) {
		MiniGameManager.serverStarted(event.getServer());
	}
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void addJsonPresetReloadListeners(GetJsonPresetListenersEvent event) {
		event.addListener(MiniGameKitsManager.get());
		event.addListener(MiniGameShopsManager.get());
	}
	
}
