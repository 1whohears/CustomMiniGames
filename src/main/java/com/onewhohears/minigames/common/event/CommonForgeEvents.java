package com.onewhohears.minigames.common.event;

import com.onewhohears.minigames.MiniGamesMod;
import com.onewhohears.minigames.data.MiniGameKitsManager;
import com.onewhohears.minigames.data.MiniGameShopsManager;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AddReloadListenerEvent;
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
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void livingDeathEvent(LivingDeathEvent event) {
		LivingEntity living = event.getEntity();
		if (living.level.isClientSide) return;
		if (living instanceof ServerPlayer player) 
			for (PlayerAgent<?> agent : MiniGameManager.get().getPlayerAgents(player)) 
				if (agent.shouldRunOnDeath()) 
					agent.onDeath(player.getServer(), event.getSource());
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
	public static void addReloadListener(AddReloadListenerEvent event) {
		event.addListener(MiniGameKitsManager.get());
		event.addListener(MiniGameShopsManager.get());
	}
	
}