package com.onewhohears.minigames.minigame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.onewhohears.minigames.minigame.data.*;
import com.onewhohears.onewholibs.util.UtilEntity;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.function.TriFunction;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import static com.onewhohears.minigames.minigame.data.MiniGameData.RED;

public class MiniGameManager extends SavedData {
	
	private static final Logger LOGGER = LogUtils.getLogger();
	private static MiniGameManager instance;
	private static final Map<String, GameGenerator> gameGenerators = new HashMap<>();
	private static final Map<String, TriFunction<ServerPlayer, PlayerAgent, CompoundTag, Boolean>> itemEvents = new HashMap<>();
	
	/**
	 * @return null if before Server Started!
	 */
	@Nullable
	public static MiniGameManager get() {
		return instance;
	}
	
	/**
	 * called in {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}
	 * register all games here
	 */
	public static void registerGames() {
		registerGame("simple_team_deathmatch", (instanceId, gameTypeId) ->
			DeathMatchData.createSimpleTeamDeathMatch(instanceId, gameTypeId, 3));
		registerGame("simple_ffa_deathmatch", (instanceId, gameTypeId) -> 
			DeathMatchData.createSimpleFFADeathMatch(instanceId, gameTypeId, 3));
		registerGame("simple_buy_attack_phases", BuyAttackData::createBuyAttackPhaseMatch);
		registerGame("simple_kill_flag", KillFlagData::createKillFlagMatch);
		registerGame("last_stand", LastStandData::createLastStandMatch);
		/*
		 * TODO 3.1 create and register the following minigame modes
		 * team capture the flag
		 * team/ffa territory control
		 * one volunteer runs away from everyone else
		 * zombie apocalypse
		 * bomb defuse
		 * hostage rescue
		 */
		// TODO 3.6.1 load custom game presets from data packs
		// TODO 3.2 clear an area of blocks before a game starts
	}
	
	/**
	 * add a game to the list of games players can play
	 * @param gameTypeId must be unique
	 * @param generator
	 * @return false if gameTypeId already exists
	 */
	public static boolean registerGame(String gameTypeId, GameGenerator generator) {
		if (generator == null) return false;
		if (gameGenerators.containsKey(gameTypeId)) return false;
		gameGenerators.put(gameTypeId, generator);
		LOGGER.debug("Registered Game "+gameTypeId);
		return true;
	}

	public static void registerItemEvents() {
		registerItemEvent("basic_summon", (player, agent, params) -> {
			String entityTypeKey = params.getString("entity");
			EntityType<?> type = UtilEntity.getEntityType(entityTypeKey, null);
			if (type == null) {
				sendError(player, "Entity Type "+entityTypeKey+" does not exist.");
				return false;
			}
			Entity entity = type.create(player.getLevel());
			if (entity == null) {
				sendError(player, "Entity couldn't be created.");
				return false;
			}
			entity.setPos(player.position());
			if (!player.getLevel().addFreshEntity(entity)) {
				sendError(player, "Entity couldn't be added to the world.");
				return false;
			}
			return true;
		});
	}

	public static void sendError(Player player, String msg) {
		player.sendSystemMessage(UtilMCText.literal(msg).setStyle(RED));
	}

	/**
	 *
	 * @param eventId a unique item event id
	 * @param consumer function should return true if the item is consumable
	 * @return true if no other event has this id
	 */
	public static boolean registerItemEvent(String eventId, TriFunction<ServerPlayer, PlayerAgent, CompoundTag, Boolean> consumer) {
		if (hasItemEvent(eventId)) return false;
		itemEvents.put(eventId, consumer);
		LOGGER.debug("Registered Item Event "+eventId);
		return true;
	}

	public static boolean hasItemEvent(String eventId) {
		return itemEvents.containsKey(eventId);
	}

	public static String[] getAllEventIds() {
		return itemEvents.keySet().toArray(new String[0]);
	}

	@Nullable
	public static TriFunction<ServerPlayer, PlayerAgent, CompoundTag, Boolean> getEventConsumer(String eventId) {
		return itemEvents.get(eventId);
	}

	public static boolean handleItemEvent(String eventId, ServerPlayer player, PlayerAgent agent, CompoundTag params) {
		return itemEvents.getOrDefault(eventId, (p, a, tag) -> false).apply(player, agent, params);
	}
	
	public static boolean hasGameType(String gameTypeId) {
		return gameGenerators.containsKey(gameTypeId);
	}
	
	public static String[] getNewGameTypeIds() {
		return gameGenerators.keySet().toArray(new String[0]);
	}
	
	public static void serverStarted(MinecraftServer server) {
		instance = server.overworld().getDataStorage().computeIfAbsent(
				MiniGameManager::load,
                MiniGameManager::new,
				"minigames");
	}
	
	public static MiniGameManager load(CompoundTag nbt) {
		MiniGameManager savedData = new MiniGameManager();
		ListTag list = nbt.getList("runningGames", 10);
		for (int i = 0; i < list.size(); ++i) {
			CompoundTag tag = list.getCompound(i);
			String gameTypeId = tag.getString("gameTypeId");
			if (!hasGameType(gameTypeId)) {
				LOGGER.error("The game type "+gameTypeId+" is not registered. Mini Game Data will be lost.");
				continue;
			}
			String gameInstanceId = tag.getString("instanceId");
			MiniGameData game = gameGenerators.get(gameTypeId).create(gameInstanceId, gameTypeId);
			game.load(tag);
			savedData.runningGames.put(gameInstanceId, game);
		}
		return savedData;
	}

	@Override
	public CompoundTag save(CompoundTag nbt) {
		ListTag list = new ListTag();
		runningGames.forEach((id, game) -> list.add(game.save()));
		nbt.put("runningGames", list);
		return nbt;
	}
	
	private final Map<String, MiniGameData> runningGames = new HashMap<>();
	
	/**
	 * add a new game to the list of running games
	 * @param gameTypeId the type of game
	 * @param gameInstanceId must be unique
	 * @return null if gameInstanceId already exists or gameTypeId doesn't exist
	 */
	@Nullable
	public MiniGameData startNewGame(String gameTypeId, String gameInstanceId) {
		if (runningGames.containsKey(gameInstanceId)) return null;
		GameGenerator gen = gameGenerators.get(gameTypeId);
		if (gen == null) return null;
		MiniGameData game = gen.create(gameInstanceId, gameTypeId);
		runningGames.put(gameInstanceId, game);
		return game;
	}
	
	public boolean resetGame(String gameInstanceId, MinecraftServer server) {
		if (!runningGames.containsKey(gameInstanceId)) return false;
		runningGames.get(gameInstanceId).reset(server);
		return true;
	}
	
	public void serverTick(MinecraftServer server) {
		runningGames.forEach((id, game) -> game.serverTick(server));
		setDirty();
	}
	
	public String[] getRunningGameIds() {
		return runningGames.keySet().toArray(new String[runningGames.size()]);
	}
	
	public boolean isGameRunning(String gameInstanceId) {
		return runningGames.containsKey(gameInstanceId);
	}
	
	@Nullable
	public MiniGameData getRunningGame(String gameInstanceId) {
		return runningGames.get(gameInstanceId);
	}
	
	public boolean removeGame(String gameInstanceId) {
		return runningGames.remove(gameInstanceId) != null;
	}

	public boolean isForceNonMemberSpectator() {
		for (MiniGameData game : runningGames.values())
			if (!game.isStopped() && !game.isSetupPhase() && game.isForceNonMemberSpectator())
				return true;
		return false;
	}

	public interface GameGenerator {
		MiniGameData create(String gameInstanceId, String gameTypeId);
	}
	
	public List<PlayerAgent> getActiveGamePlayerAgents(ServerPlayer player) {
		List<PlayerAgent> agents = new ArrayList<>();
		for (MiniGameData game : runningGames.values()) {
			if (game.isPausedOrStopped()) continue;
			PlayerAgent agent = game.getPlayerAgentByUUID(player.getStringUUID());
			if (agent != null) agents.add(agent);
		}
		return agents;
	}

	/**
	 * @return true if items should be consumed
	 */
	public boolean onEventItemUse(ServerPlayer player, String event, CompoundTag tag) {
		boolean consume = false;
		List<PlayerAgent> agents = getActiveGamePlayerAgents(player);
		for (PlayerAgent agent : agents) {
			if (!agent.getGameData().canHandleEvent(event)) {
				player.sendSystemMessage(UtilMCText.literal("The game "
						+agent.getGameData().getInstanceId()+" does not use event type "+event));
				continue;
			}
			if (agent.getGameData().handleEvent(player, agent, event, tag)) {
				consume = true;
			}
		}
		return consume;
	}
	
}
