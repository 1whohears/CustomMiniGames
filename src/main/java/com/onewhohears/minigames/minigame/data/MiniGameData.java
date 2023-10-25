package com.onewhohears.minigames.minigame.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.agent.TeamAgent;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import com.onewhohears.minigames.minigame.phase.SetupPhase;
import com.onewhohears.minigames.util.UtilParse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

public abstract class MiniGameData {
	
	private final String gameTypeId;
	private final String instanceId;
	private final Map<String, GameAgent<?>> agents = new HashMap<>();
	private final Map<String, GamePhase<?>> phases = new HashMap<>();
	private SetupPhase<?> setupPhase;
	private GamePhase<?> nextPhase;
	private GamePhase<?> currentPhase;
	private int age;
	private boolean isStarted, isStopped;
	
	protected boolean canAddIndividualPlayers, canAddTeams;
	protected boolean requiresSetRespawnPos, worldBorderDuringGame;
	protected int initialLives = 3;
	protected double gameBorderSize = 1000;
	protected Vec3 gameCenter = Vec3.ZERO;
	
	protected MiniGameData(String instanceId, String gameTypeId) {
		this.instanceId = instanceId;
		this.gameTypeId = gameTypeId;
	}
	
	public CompoundTag save() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("gameTypeId", gameTypeId);
		nbt.putString("instanceId", instanceId);
		nbt.putInt("age", age);
		nbt.putBoolean("isStarted", isStarted);
		nbt.putBoolean("isStopped", isStopped);
		nbt.putBoolean("canAddIndividualPlayers", canAddIndividualPlayers);
		nbt.putBoolean("canAddTeams", canAddTeams);
		nbt.putBoolean("requiresSetRespawnPos", requiresSetRespawnPos);
		nbt.putBoolean("worldBorderDuringGame", worldBorderDuringGame);
		nbt.putInt("initialLives", initialLives);
		nbt.putDouble("gameBorderSize", gameBorderSize);
		UtilParse.writeVec3(nbt, gameCenter, "gameCenter");
		saveAgents(nbt);
		savePhases(nbt);
		return nbt;
	}
	
	public void load(CompoundTag nbt) {
		age = nbt.getInt("age");
		isStarted = nbt.getBoolean("isStarted");
		isStopped = nbt.getBoolean("isStopped");
		canAddIndividualPlayers = nbt.getBoolean("canAddIndividualPlayers");
		canAddTeams = nbt.getBoolean("canAddTeams");
		requiresSetRespawnPos = nbt.getBoolean("requiresSetRespawnPos");
		worldBorderDuringGame = nbt.getBoolean("worldBorderDuringGame");
		initialLives = nbt.getInt("initialLives");
		gameBorderSize = nbt.getDouble("gameBorderSize");
		gameCenter = UtilParse.readVec3(nbt, "gameCenter");
		loadAgents(nbt);
		loadPhases(nbt);
	}
	
	protected void saveAgents(CompoundTag nbt) {
		ListTag agentList = new ListTag();
		agents.forEach((id, agent) -> agentList.add(agent.save()));
		nbt.put("agentList", agentList);
	}
	
	protected void loadAgents(CompoundTag nbt) {
		ListTag agentList = nbt.getList("agentList", 10);
		for (int i = 0; i < agentList.size(); ++i) {
			CompoundTag tag = agentList.getCompound(i);
			String id = tag.getString("id");
			GameAgent<?> agent;
			if (tag.getBoolean("isPlayer")) agent = createPlayerAgent(id);	
			else if (tag.getBoolean("isTeam")) agent = createTeamAgent(id);
			else continue;
			agent.load(tag);
			agents.put(id, agent);
		}
	}
	
	protected void savePhases(CompoundTag nbt) {
		ListTag phaseList = new ListTag();
		phases.forEach((id, phase) -> phaseList.add(phase.save()));
		nbt.put("phaseList", phaseList);
	}
	
	protected void loadPhases(CompoundTag nbt) {
		ListTag phaseList = nbt.getList("phaseList", 10);
		for (int i = 0; i < phaseList.size(); ++i) {
			CompoundTag tag = phaseList.getCompound(i);
			String id = tag.getString("id");
			GamePhase<?> phase = phases.get(id);
			if (phase == null) continue;
			phase.load(tag);
		}
	}
	
	/**
	 * must be called inside a {@link com.onewhohears.minigames.minigame.MiniGameManager.GameGenerator}
	 */
	protected void setPhases(SetupPhase<?> setupPhase, GamePhase<?> nextPhase, GamePhase<?>...otherPhases) {
		this.setupPhase = setupPhase;
		this.nextPhase = nextPhase;
		phases.put(setupPhase.getId(), setupPhase);
		phases.put(nextPhase.getId(), nextPhase);
		for (GamePhase<?> phase : otherPhases) phases.put(phase.getId(), phase);
		currentPhase = setupPhase;
	}
	
	public void serverTick(MinecraftServer server) {
		if (!isStarted() && shouldStart(server)) start(server);
		if (isStarted() && !isStopped() && shouldStop(server)) stop(server);
 		if (shouldTickGame(server)) tickGame(server);
	}
	
	public void tickGame(MinecraftServer server) {
		++age;
		currentPhase.tickPhase(server);
		agents.forEach((id, agent) -> { 
			if (agent.canTickAgent(server)) 
				tickAgent(server, agent); 
		});
	}
	
	protected void tickAgent(MinecraftServer server, GameAgent<?> agent) {
		agent.tickAgent(server);
		if (agent.isPlayer()) currentPhase.tickPlayerAgent(server, (PlayerAgent<?>) agent);
		else if (agent.isTeam()) currentPhase.tickTeamAgent(server, (TeamAgent<?>) agent);
 	}
	
	public boolean changePhase(MinecraftServer server, String phaseId) {
		if (!phases.containsKey(phaseId)) return false;
		System.out.println("GAME CHANGE PHASE "+instanceId+" to "+phaseId);
		currentPhase = phases.get(phaseId);
		currentPhase.onReset(server);
		currentPhase.onStart(server);
		return true;
	}
	
	public boolean finishSetupPhase(MinecraftServer server) {
		if (!isSetupPhase()) return false;
		if (!canFinishSetupPhase(server)) return false;
		currentPhase.onStop(server);
		if (requiresSetRespawnPos()) applyAllAgentRespawnPoints(server);
		return changePhase(server, nextPhase.getId());
	}
	
	public boolean canFinishSetupPhase(MinecraftServer server) {
		return agents.size() >= 2 && areAgentRespawnPosSet();
	}
	
	public void reset(MinecraftServer server) {
		System.out.println("GAME RESET "+instanceId);
		isStarted = false;
		isStopped = false;
		age = 0;
		agents.clear();
		phases.forEach((id, phase) -> phase.onReset(server));
	}
	
	public void start(MinecraftServer server) {
		System.out.println("GAME START "+instanceId);
		isStarted = true;
		isStopped = false;
		resetAllAgents();
		changePhase(server, setupPhase.getId());
	}
	
	public void stop(MinecraftServer server) {
		System.out.println("GAME STOP "+instanceId);
		isStarted = true;
		isStopped = true;
	}
	
	public boolean shouldTickGame(MinecraftServer server) {
		return isStarted() && !isStopped();
	}
	
	public boolean isSetupPhase() {
		return isStarted() && currentPhase.isSetupPhase();
	}
	
	public boolean shouldStart(MinecraftServer server) {
		return true;
	}
	
	public boolean shouldStop(MinecraftServer server) {
		return false;
	}
	
	public boolean isStarted() {
		return isStarted;
	}
	
	public boolean isStopped() {
		return isStopped;
	}
	
	public String getInstanceId() {
		return instanceId;
	}
	
	public String getGameTypeId() {
		return gameTypeId;
	}
	
	public int getAge() {
		return age;
	}
	
	public boolean canAddIndividualPlayers() {
		return canAddIndividualPlayers;
	}
	
	public boolean canAddTeams() {
		return canAddTeams;
	}
	
	public int getInitialLives() {
		return initialLives;
	}
	
	public void setInitialLives(int lives) {
		this.initialLives = lives;
	}
	
	public void setGameCenter(Vec3 center) {
		gameCenter = center;
	}
	
	public void setGameCenter(Vec3 center, MinecraftServer server) {
		setGameCenter(center);
		currentPhase.updateWorldBorder(server);
	}
	
	public Vec3 getGameCenter() {
		return gameCenter;
	}
	
	public double getGameBorderSize() {
		return gameBorderSize;
	}
	
	public void setGameBorderSize(double size) {
		gameBorderSize = size;
	}
	
	public GamePhase<?> getCurrentPhase() {
		return currentPhase;
	}
	
	public boolean requiresSetRespawnPos() {
		return requiresSetRespawnPos;
	}
	
	public boolean useWorldBorderDuringGame() {
		return worldBorderDuringGame;
	}
	
	public void setUseWorldBorderDuringGame(boolean use) {
		this.worldBorderDuringGame = use;
	}
	
	public boolean areAgentRespawnPosSet() {
		if (!requiresSetRespawnPos()) return true;
		for (GameAgent<?> agent : agents.values()) 
			if (!agent.hasRespawnPoint()) 
				return false;
		return true;
	}
	
	public String getSetupInfo() {
		String info = "use set_center to set the middle of the game. ";
		info += "\nuse set_use_border to set if the world border is used during gameplay phase. ";
		info += "\nuse set_size to set the game world border size and random start position distance. ";
		info += "\nuse set_lives to set the number of initial lives. ";
		if (canAddIndividualPlayers()) info += "\nuse add_player to add players to the game. ";
		if (canAddTeams()) info += "\nuse add_team to add teams to the game. ";
		if (requiresSetRespawnPos()) info += "\nuse set_spawn to set a player or team spawnpoint. ";
		return info;
	}
	
	@SuppressWarnings("unchecked")
	public <D extends MiniGameData> PlayerAgent<D> createPlayerAgent(String uuid) {
		return new PlayerAgent<D>(uuid, (D) this);
	}
	
	public <D extends MiniGameData> PlayerAgent<D> createPlayerAgent(ServerPlayer player) {
		return createPlayerAgent(player.getStringUUID());
	}
	
	@SuppressWarnings("unchecked")
	public <D extends MiniGameData> TeamAgent<D> createTeamAgent(String teamName) {
		return new TeamAgent<D>(teamName, (D)this);
	}
	
	public <D extends MiniGameData> TeamAgent<D> createTeamAgent(PlayerTeam team) {
		return createTeamAgent(team.getName());
	}
	
	@Nullable
	public PlayerAgent<?> getAddIndividualPlayer(ServerPlayer player) {
		if (!canAddIndividualPlayers()) return null;
		PlayerAgent<?> agent = getPlayerAgentByUUID(player.getStringUUID());
		if (agent == null) agent = createPlayerAgent(player);
		return agent;
	}
	
	@Nullable
	public TeamAgent<?> getAddTeam(PlayerTeam team) {
		if (!canAddTeams()) return null;
		TeamAgent<?> agent = getTeamAgentByName(team.getName());
		if (agent == null) agent = createTeamAgent(team);
		return agent;
	}
	
	public boolean hasAgentById(String id) {
		return agents.containsKey(id);
	}
	
	@Nullable
	public GameAgent<?> getAgentById(String id) {
		return agents.get(id);
	}
	
	public boolean removeAgentById(String id) {
		return agents.remove(id) != null;
	}
	
	@Nullable
	public PlayerAgent<?> getPlayerAgentByUUID(String uuid) {
		if (canAddIndividualPlayers()) {
			GameAgent<?> agent = getAgentById(uuid);
			if (agent != null && agent.isPlayer()) return (PlayerAgent<?>) agent;
		}
		if (canAddTeams()) {
			for (GameAgent<?> agent : agents.values()) if (agent.isTeam()) {
				TeamAgent<?> team = (TeamAgent<?>)agent;
				PlayerAgent<?> player = team.getPlayerAgentByUUID(uuid);
				if (player != null) return player;
			}
		}
		return null;
	}
	
	@Nullable
	public TeamAgent<?> getTeamAgentByName(String name) {
		if (!canAddTeams()) return null;
		GameAgent<?> agent = getAgentById(name);
		if (agent == null) return null;
		if (agent.isTeam()) return (TeamAgent<?>) agent;
		return null;
	}
	
	@Nullable
	public TeamAgent<?> getPlayerTeamAgent(ServerPlayer player) {
		if (!canAddTeams()) return null;
		Team team = player.getTeam();
		if (team == null) return null;
		return getTeamAgentByName(team.getName());
	}
	
	@Nullable
	public TeamAgent<?> getPlayerTeamAgent(String uuid) {
		if (!canAddTeams()) return null;
		for (GameAgent<?> agent : agents.values()) if (agent.isTeam()) {
			TeamAgent<?> team = (TeamAgent<?>)agent;
			if (team.getPlayerAgentByUUID(uuid) != null) 
				return team;
		}
		return null;
	}
	
	public List<GameAgent<?>> getLivingAgents() {
		List<GameAgent<?>> living = new ArrayList<>();
		for (GameAgent<?> agent : agents.values()) 
			if (!agent.isDead()) living.add(agent);
		return living;
	}
	
	public List<GameAgent<?>> getDeadAgents() {
		List<GameAgent<?>> dead = new ArrayList<>();
		for (GameAgent<?> agent : agents.values()) 
			if (agent.isDead()) dead.add(agent);
		return dead;
	}
	
	public List<PlayerAgent<?>> getAllPlayerAgents() {
		List<PlayerAgent<?>> players = new ArrayList<>();
		for (GameAgent<?> agent : agents.values()) {
			if (agent.isPlayer()) players.add((PlayerAgent<?>) agent);
			else if (agent.isTeam()) {
				TeamAgent<?> team = (TeamAgent<?>) agent;
				for (PlayerAgent<?> player : team.getPlayerAgents()) 
					players.add(player);
			}
		}
		return players;
	}
	
	public void resetAllAgents() {
		agents.forEach((id, agent) -> {
			agent.resetAgent();
		});
	}
	
	public void applyAllAgentRespawnPoints(MinecraftServer server) {
		agents.forEach((id, agent) -> {
			agent.applySpawnPoint(server);
		});
	}
	
	public void tpPlayersToSpawnPosition(MinecraftServer server) {
		agents.forEach((id, agent) -> {
			agent.tpToSpawnPoint(server);
		});
	}
	
	public void spreadPlayers(MinecraftServer server) {
		// TODO 3.7 spread players at start of game option
	}
	
	public void announceWinners(MinecraftServer server) {
		List<GameAgent<?>> winners = getLivingAgents();
		if (winners.size() != 1) return;
		GameAgent<?> winner = winners.get(0);
		winner.onWin(server);
	}
	
	public void chatToAllPlayers(MinecraftServer server, Component message) {
		for (PlayerAgent<?> agent : getAllPlayerAgents()) {
			ServerPlayer player = agent.getPlayer(server);
			if (player == null) continue;
			player.displayClientMessage(message, false);
		}
	}
	
}
