package com.onewhohears.minigames.common.event;

import com.onewhohears.minigames.entity.FlagEntity;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.minigames.util.CMGUtil;
import com.onewhohears.onewholibs.common.event.GetJsonPresetListenersEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.onewhohears.minigames.MiniGamesMod;
import com.onewhohears.minigames.data.kits.MiniGameKitsManager;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.util.List;

@Mod.EventBusSubscriber(modid = MiniGamesMod.MODID, bus = Bus.FORGE)
public class CommonForgeEvents {
	
	private static final Logger LOGGER = LogUtils.getLogger();

	@SubscribeEvent
	public static void livingHurtEvent(LivingHurtEvent event) {
		if (event.getEntity().getLevel().isClientSide()) return;
		if (event.getEntity() instanceof ServerPlayer player) {
			for (PlayerAgent agent : MiniGameManager.get().getActiveGamePlayerAgents(player)) {
				if (!agent.getGameData().getCurrentPhase().allowPVP()) {
					event.setCanceled(true);
					return;
				}
			}
		} else if (event.getEntity() instanceof FlagEntity flag) {
			MiniGameData data = flag.getGameData();
			if (data == null) return;
			if (data.getCurrentPhase().allowPVP()) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void livingDeathEvent(LivingDeathEvent event) {
		if (event.getEntity().getLevel().isClientSide()) return;
		if (event.getEntity() instanceof FlagEntity flag) {
			flag.onDeath(event.getSource());
			return;
		}
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		for (PlayerAgent agent : MiniGameManager.get().getActiveGamePlayerAgents(player)) {
			if (agent.shouldRunOnDeath()) {
				agent.setDeathPosition(player.position());
				agent.setDeathLookDirection(player.getXRot(), player.getYRot());
				agent.onDeath(player.getServer(), event.getSource());
			}
		}
	}

	@SubscribeEvent
	public static void playerPlaceBlock(BlockEvent.EntityPlaceEvent event) {
		if (event.getEntity() == null) return;
		if (event.getEntity().getLevel().isClientSide()) return;
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		BlockItem blockItem = null;
		InteractionHand hand = InteractionHand.MAIN_HAND;
		if (player.getUsedItemHand() == InteractionHand.MAIN_HAND &&
				player.getMainHandItem().getItem() instanceof BlockItem item) {
			blockItem = item;
		} else if (player.getOffhandItem().getItem() instanceof BlockItem item) {
			blockItem = item;
			hand = InteractionHand.OFF_HAND;
		}
		if (blockItem == null) return;
		for (PlayerAgent agent : MiniGameManager.get().getActiveGamePlayerAgents(player)) {
			if (!agent.allowBlockPlace(player.getServer(), event.getPos(), blockItem.getBlock())) {
				event.setCanceled(true);
				CMGUtil.forceHeldItemSync(player, hand);
				return;
			}
		}
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
		List<PlayerAgent> agents = MiniGameManager.get().getActiveGamePlayerAgents(player);
		if (agents.isEmpty() && MiniGameManager.get().isForceNonMemberSpectator()) {
			player.setGameMode(GameType.SPECTATOR);
		} else {
			for (PlayerAgent agent : agents)
				if (agent.shouldRunOnRespawn())
					agent.onLogIn(player.getServer());
		}
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
