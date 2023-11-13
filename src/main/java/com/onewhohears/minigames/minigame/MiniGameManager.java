package com.onewhohears.minigames.minigame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.data.DeathMatchData;
import com.onewhohears.minigames.minigame.data.MiniGameData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

public class MiniGameManager extends SavedData {
	
	private static final Logger LOGGER = LogUtils.getLogger();
	private static MiniGameManager instance;
	private static Map<String, GameGenerator> gameGenerators = new HashMap<>();
	
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
		/*
		 * TODO 3.1 create and register the following minigame modes
		 * team/ffa death match (1 or multiple lives)
		 * team capture the flag
		 * team/ffa territory control
		 * one volunteer runs away from everyone else
		 * zombie apocalypse
		 * bomb defuse
		 * hostage rescue
		 */
		// TODO 3.6.1 load custom game presets from data packs
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
	
	public static boolean hasGameType(String gameTypeId) {
		return gameGenerators.containsKey(gameTypeId);
	}
	
	public static String[] getNewGameTypeIds() {
		return gameGenerators.keySet().toArray(new String[gameGenerators.size()]);
	}
	
	public static void serverStarted(MinecraftServer server) {
		instance = server.overworld().getDataStorage().computeIfAbsent(
				MiniGameManager::load, 
				() -> new MiniGameManager(), 
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
	
	private Map<String, MiniGameData> runningGames = new HashMap<>();
	
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
	
	public String[] getRunningeGameIds() {
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
	
	public interface GameGenerator {
		MiniGameData create(String gameInstanceId, String gameTypeId);
	}
	
	public List<PlayerAgent<?>> getActiveGamePlayerAgents(ServerPlayer player) {
		List<PlayerAgent<?>> agents = new ArrayList<>();
		for (MiniGameData game : runningGames.values()) {
			if (game.isStopped()) continue;
			PlayerAgent<?> agent = game.getPlayerAgentByUUID(player.getStringUUID());
			if (agent != null) agents.add(agent);
		}
		return agents;
	}
	
}
