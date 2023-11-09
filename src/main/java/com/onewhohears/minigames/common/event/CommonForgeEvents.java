package com.onewhohears.minigames.common.event;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.onewhohears.minigames.MiniGamesMod;
import com.onewhohears.minigames.common.network.PacketHandler;
import com.onewhohears.minigames.common.network.toclient.ToClientDataPackSynch;
import com.onewhohears.minigames.data.kits.MiniGameKitsManager;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;

@Mod.EventBusSubscriber(modid = MiniGamesMod.MODID, bus = Bus.FORGE)
public class CommonForgeEvents {
	
	private static final Logger LOGGER = LogUtils.getLogger();
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void livingDeathEvent(LivingDeathEvent event) {
		LivingEntity living = event.getEntity();
		if (living.level.isClientSide) return;
		if (living instanceof ServerPlayer player) 
			for (PlayerAgent<?> agent : MiniGameManager.get().getActiveGamePlayerAgents(player)) 
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
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void onDatapackSync(OnDatapackSyncEvent event) {
		LOGGER.debug("DATAPACK SYNC "+event.getPlayer());
		PacketTarget target;
		if (event.getPlayer() == null) target = PacketDistributor.ALL.noArg();
		else target = PacketDistributor.PLAYER.with(() -> event.getPlayer());
		PacketHandler.INSTANCE.send(target, new ToClientDataPackSynch());
	}
	
}
