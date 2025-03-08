package com.onewhohears.minigames.minigame.data;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.onewhohears.minigames.entity.FlagEntity;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.VanillaTeamAgent;
import com.onewhohears.minigames.minigame.param.MiniGameParamHolder;
import com.onewhohears.minigames.minigame.param.MiniGameParamType;
import com.onewhohears.minigames.minigame.poi.GamePOI;
import com.onewhohears.onewholibs.util.UtilMCText;
import com.onewhohears.onewholibs.util.math.UtilAngles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.agent.TeamAgent;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import com.onewhohears.minigames.minigame.phase.SetupPhase;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

import static com.onewhohears.minigames.minigame.param.MiniGameParamTypes.*;

public abstract class MiniGameData {

	public static final Style GOLD_BOLD = Style.EMPTY.withBold(true)
			.withUnderlined(true).withColor(ChatFormatting.GOLD);
	public static final Style AQUA = Style.EMPTY.withColor(ChatFormatting.AQUA);
	public static final Style GREEN_BOLD = Style.EMPTY.withBold(true)
			.withUnderlined(true).withColor(ChatFormatting.DARK_GREEN);
	public static final Style GREEN = Style.EMPTY.withColor(ChatFormatting.GREEN);
	public static final Style RED = Style.EMPTY.withColor(ChatFormatting.RED);

	protected static final Logger LOGGER = LogUtils.getLogger();
	
	private final String gameTypeId;
	private final String instanceId;
	private final Map<MiniGameParamType<?>, MiniGameParamHolder<?,?>> params = new HashMap<>();
	private final Map<String, GameAgent> agents = new HashMap<>();
	private final Map<String, GamePhase<?>> phases = new HashMap<>();
	private final Map<String, GamePOI<?>> pois = new HashMap<>();
	private final Set<FlagEntity> flags = new HashSet<>();
	private SetupPhase<?> setupPhase;
	private GamePhase<?> nextPhase;
	private GamePhase<?> currentPhase;
	private int age, resets;
	private boolean isStarted, isStopped, isPaused, firstTick = true;
	
	protected MiniGameData(String instanceId, String gameTypeId) {
		this.instanceId = instanceId;
		this.gameTypeId = gameTypeId;
		registerParams();
	}
	
	public CompoundTag save() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("gameTypeId", gameTypeId);
		nbt.putString("instanceId", instanceId);
		nbt.putInt("age", age);
		nbt.putInt("resets", resets);
		nbt.putBoolean("isStarted", isStarted);
		nbt.putBoolean("isStopped", isStopped);
		nbt.putBoolean("isPaused", isPaused);
		saveParams(nbt);
		saveAgents(nbt);
		savePhases(nbt);
		savePois(nbt);
		return nbt;
	}
	
	public void load(CompoundTag nbt) {
		age = nbt.getInt("age");
		resets = nbt.getInt("resets");
		isStarted = nbt.getBoolean("isStarted");
		isStopped = nbt.getBoolean("isStopped");
		isPaused = nbt.getBoolean("isPaused");
		loadParams(nbt);
		loadAgents(nbt);
		loadPhases(nbt);
		loadPois(nbt);
	}

	protected void saveParams(CompoundTag nbt) {
		params.forEach((type, holder) -> holder.save(nbt));
	}

	protected void loadParams(CompoundTag nbt) {
		params.forEach((type, holder) -> holder.load(nbt));
	}

	protected void savePois(CompoundTag nbt) {
		ListTag agentList = new ListTag();
		pois.forEach((id, poi) -> agentList.add(poi.save()));
		nbt.put("poiList", agentList);
	}

	protected void loadPois(CompoundTag nbt) {
		ListTag agentList = nbt.getList("poiList", 10);
		for (int i = 0; i < agentList.size(); ++i) {
			CompoundTag tag = agentList.getCompound(i);
			String typeId = tag.getString("typeId");
			String instanceId = tag.getString("instanceId");
			GamePOI<?> poi = MiniGameManager.createGamePOI(typeId, instanceId, this);
			if (poi == null) continue;
			poi.load(tag);
			pois.put(instanceId, poi);
		}
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
			String type = tag.getString("type");
			if (type.isEmpty()) {
				if (tag.getBoolean("isPlayer")) type = "player";
				else if (tag.getBoolean("isTeam")) type = "vanilla_team";
				else continue;
			}
			String id = tag.getString("id");
			GameAgent agent = MiniGameManager.createGameAgent(type, id, this);
			if (agent == null) continue;
			agent.load(tag);
			agents.put(id, agent);
		}
	}
	
	protected void savePhases(CompoundTag nbt) {
		ListTag phaseList = new ListTag();
		phases.forEach((id, phase) -> phaseList.add(phase.save()));
		nbt.put("phaseList", phaseList);
		nbt.putString("currentPhase", currentPhase.getId());
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
		String currentPhaseId = nbt.getString("currentPhase");
		GamePhase<?> phase = phases.get(currentPhaseId);
		if (phase != null) currentPhase = phase;
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
		//System.out.println("GAME TICK: id="+getInstanceId()+" start="+isStarted()+" stop="+isStopped());
		if (!isStarted() && shouldStart(server)) start(server);
		if (isStarted() && !isStopped() && shouldStop(server)) stop(server);
		if (shouldTickGame(server)) tickGame(server);
		firstTick = false;
	}
	
	public void tickGame(MinecraftServer server) {
		++age;
		getCurrentPhase().tickPhase(server);
		agents.forEach((id, agent) -> { 
			if (agent.canTickAgent(server)) 
				tickAgent(server, agent); 
		});
		pois.forEach((id, poi) -> {
			if (poi.canTick(server))
				poi.tick(server);
		});
	}
	
	protected void tickAgent(MinecraftServer server, GameAgent agent) {
		agent.tickAgent(server);
		if (agent.isPlayer()) getCurrentPhase().tickPlayerAgent(server, (PlayerAgent) agent);
		else if (agent.isTeam()) getCurrentPhase().tickTeamAgent(server, (TeamAgent) agent);
 	}
	
	public boolean changePhase(MinecraftServer server, String phaseId) {
		if (!phases.containsKey(phaseId)) return false;
        LOGGER.debug("GAME CHANGE PHASE {} to {}", instanceId, phaseId);
		currentPhase = phases.get(phaseId);
		currentPhase.onReset(server);
		currentPhase.onStart(server);
		return true;
	}

	@Nullable
	public final String getStartFailedReason(MinecraftServer server) {
		if (!isSetupPhase()) return "Game isn't in setup phase, need to reset the game to start a new one.";
		if (agents.size() < 2) return "There must be at least 2 players!";
		if (!areAgentRespawnPosSet()) return "Must set respawn positions for each team!";
		String other = getAdditionalStartFailReasons(server);
		if (other != null) return other;
		getCurrentPhase().onStop(server);
		setupAllAgents();
		if (requiresSetRespawnPos()) applyAllAgentRespawnPoints(server);
		if (!changePhase(server, nextPhase.getId())) return "Failed to change phase. Contact developer.";
		return null;
	}

	@Nullable
	protected String getAdditionalStartFailReasons(MinecraftServer server) {
		return null;
	}
	
	public void reset(MinecraftServer server) {
        LOGGER.debug("GAME RESET {}", instanceId);
		isStarted = false;
		isStopped = false;
		age = 0;
		++resets;
		resetAllAgents();
		phases.forEach((id, phase) -> phase.onReset(server));
		discardAllFlags();
	}

	public boolean pause(MinecraftServer server) {
		if (!canPause(server)) return false;
		isPaused = true;
		return true;
	}

	public boolean canPause(MinecraftServer server) {
		return isStarted() && !isStopped();
	}

	public boolean resume(MinecraftServer server) {
		if (!canResume(server)) return false;
		isPaused = false;
		return true;
	}

	public boolean canResume(MinecraftServer server) {
		return isStarted() && !isStopped();
	}

	public boolean isPaused() {
		return isPaused;
	}

	public void discardAllFlags() {
		flags.forEach(Entity::discard);
		flags.clear();
	}

	public int getNumResets() {
		return resets;
	}
	
	public void start(MinecraftServer server) {
        LOGGER.debug("GAME START {}", instanceId);
		isStarted = true;
		isPaused = false;
		isStopped = false;
		changePhase(server, setupPhase.getId());
	}
	
	public void stop(MinecraftServer server) {
        LOGGER.debug("GAME STOP {}", instanceId);
		isStarted = true;
		isPaused = false;
		isStopped = true;
		getCurrentPhase().onStop(server);
	}
	
	public boolean shouldTickGame(MinecraftServer server) {
		return isStarted() && !isPausedOrStopped();
	}
	
	public boolean isSetupPhase() {
		return isStarted() && getCurrentPhase().isSetupPhase();
	}
	
	public boolean shouldStart(MinecraftServer server) {
		return true;
	}
	
	public boolean shouldStop(MinecraftServer server) {
		return getCurrentPhase().shouldEndGame();
	}
	
	public boolean isStarted() {
		return isStarted;
	}
	
	public boolean isStopped() {
		return isStopped;
	}

	public boolean isPausedOrStopped() {
		return isPaused() || isStopped();
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
		return getBooleanParam(CAN_ADD_PLAYERS);
	}
	
	public boolean canAddTeams() {
		return getBooleanParam(CAN_ADD_TEAMS);
	}
	
	public int getDefaultInitialLives() {
		return getIntParam(DEFAULT_LIVES);
	}
	
	public void setGameCenter(@NotNull Vec3 center) {
		setParam(GAME_CENTER, center);
	}
	
	public void setGameCenter(@NotNull Vec3 center, MinecraftServer server) {
		setGameCenter(center);
		getCurrentPhase().updateWorldBorder(server);
	}

	@NotNull
	public Vec3 getGameCenter() {
		return getVec3Param(GAME_CENTER);
	}
	
	public double getGameBorderSize() {
		return getDoubleParam(WORLD_BORDER_SIZE);
	}
	
	public GamePhase<?> getCurrentPhase() {
		return currentPhase;
	}
	
	public boolean requiresSetRespawnPos() {
		return getBooleanParam(REQUIRE_SET_SPAWN);
	}
	
	public boolean useWorldBorderDuringGame() {
		return getBooleanParam(USE_WORLD_BORDER);
	}
	
	public boolean areAgentRespawnPosSet() {
		if (!requiresSetRespawnPos()) return true;
		for (GameAgent agent : agents.values())
			if (!agent.hasRespawnPoint()) 
				return false;
		return true;
	}

	protected Set<String> getEnabledKits() {
		return getStringSetParam(KITS);
	}

	public void addKits(String... ids) {
		getEnabledKits().addAll(Arrays.asList(ids));
	}
	
	public void removeKit(String id) {
		getEnabledKits().remove(id);
	}
	
	public String[] getEnabledKitIds() {
		return getEnabledKits().toArray(new String[0]);
	}

	public boolean hasKit(String id) {
		return getEnabledKits().contains(id);
	}

	protected Set<String> getEnabledShops() {
		return getStringSetParam(SHOPS);
	}

	public void addShops(String... ids) {
        Collections.addAll(getEnabledShops(), ids);
	}
	
	public void removeShops(String... ids) {
		for (String id : ids) getEnabledShops().remove(id);
	}
	
	public String[] getEnabledShopIds() {
		return getEnabledShops().toArray(new String[0]);
	}

	public boolean hasShop(String id) {
		return getEnabledShops().contains(id);
	}
	
	public boolean isFirstTick() {
		return firstTick;
	}
	
	public String getSetupInfo() {
		String info = "use set_center to set the middle of the game. ";
		info += "\nuse set_use_border to set if the world border is used during gameplay phase. ";
		info += "\nuse set_size to set the game world border size and random start position distance. ";
		info += "\nuse set_lives to set the number of initial lives. ";
		info += "\nuse clear_on_start to set if player inventories should be cleared should be cleared on start.";
		if (canAddIndividualPlayers()) info += "\nuse add_player to add players to the game. ";
		if (canAddTeams()) info += "\nuse add_team to add teams to the game. ";
		if (requiresSetRespawnPos()) info += "\nuse set_spawn to set a player or team spawnpoint. ";
		return info;
	}

	public String getDefaultPlayerAgentType() {
		return "player";
	}

	public String getDefaultTeamAgentType() {
		return "vanilla_team";
	}

	public PlayerAgent createPlayerAgent(String uuid) {
		return new PlayerAgent(getDefaultPlayerAgentType(), uuid, this);
	}
	
	public PlayerAgent createPlayerAgent(ServerPlayer player) {
		return createPlayerAgent(player.getStringUUID());
	}

	public TeamAgent createTeamAgent(String teamName) {
		return new VanillaTeamAgent(getDefaultTeamAgentType(), teamName, this);
	}
	
	public TeamAgent createTeamAgent(PlayerTeam team) {
		return createTeamAgent(team.getName());
	}
	
	@Nullable
	public PlayerAgent getAddIndividualPlayer(ServerPlayer player) {
		if (!canAddIndividualPlayers()) return null;
		PlayerAgent agent = getPlayerAgentByUUID(player.getStringUUID());
		if (agent == null) {
			agent = createPlayerAgent(player);
			agents.put(agent.getId(), agent);
		}
		return agent;
	}
	
	@Nullable
	public TeamAgent getAddTeam(PlayerTeam team, boolean override) {
		if (!override && !canAddTeams()) return null;
		TeamAgent agent = getTeamAgentByName(team.getName());
		if (agent == null) {
			agent = createTeamAgent(team);
			agents.put(agent.getId(), agent);
		}
		return agent;
	}
	
	@Nullable
	public TeamAgent getAddTeam(PlayerTeam team) {
		return getAddTeam(team, false);
	}

	@Nullable
	public GameAgent getAddAgent(String type, String name) {
		GameAgent agent = getAgentById(name);
		if (agent == null) {
			agent = MiniGameManager.createGameAgent(type, name, this);
			if (agent == null) return null;
			if (agent.isTeam() && !canAddTeams()) return null;
			else if (agent.isPlayer() && !canAddIndividualPlayers()) return null;
			agents.put(agent.getId(), agent);
		}
		return agent;
	}

	@Nullable
	public TeamAgent getAddTeam(String type, String name) {
		GameAgent agent = getAddAgent(type, name);
		if (agent == null || !agent.isTeam()) return null;
		return (TeamAgent) agent;
	}

	public boolean hasAgentById(String id) {
		return agents.containsKey(id);
	}
	
	@Nullable
	public GameAgent getAgentById(String id) {
		return agents.get(id);
	}
	
	public boolean removeAgentById(String id) {
		return agents.remove(id) != null;
	}
	
	@Nullable
	public PlayerAgent getPlayerAgentByUUID(String uuid) {
		if (canAddIndividualPlayers()) {
			GameAgent agent = getAgentById(uuid);
			if (agent != null && agent.isPlayer()) return (PlayerAgent) agent;
		}
		if (canAddTeams()) {
			for (GameAgent agent : agents.values()) if (agent.isTeam()) {
				TeamAgent team = (TeamAgent)agent;
				PlayerAgent player = team.getPlayerAgentByUUID(uuid);
				if (player != null) return player;
			}
		}
		return null;
	}
	
	@Nullable
	public TeamAgent getTeamAgentByName(String name) {
		if (!canAddTeams()) return null;
		GameAgent agent = getAgentById(name);
		if (agent == null) return null;
		if (agent.isTeam()) return (TeamAgent) agent;
		return null;
	}
	
	@Nullable
	public TeamAgent getPlayerTeamAgent(ServerPlayer player) {
		if (!canAddTeams()) return null;
		Team team = player.getTeam();
		if (team == null) return null;
		return getTeamAgentByName(team.getName());
	}
	
	@Nullable
	public TeamAgent getPlayerTeamAgent(String uuid) {
		if (!canAddTeams()) return null;
		for (GameAgent agent : agents.values()) if (agent.isTeam()) {
			TeamAgent team = (TeamAgent)agent;
			if (team.getPlayerAgentByUUID(uuid) != null) 
				return team;
		}
		return null;
	}
	
	public List<GameAgent> getLivingAgents() {
		List<GameAgent> living = new ArrayList<>();
		for (GameAgent agent : agents.values())
			if (!agent.isDead()) living.add(agent);
		return living;
	}
	
	public List<GameAgent> getDeadAgents() {
		List<GameAgent> dead = new ArrayList<>();
		for (GameAgent agent : agents.values())
			if (agent.isDead()) dead.add(agent);
		return dead;
	}
	
	public List<PlayerAgent> getAllPlayerAgents() {
		List<PlayerAgent> players = new ArrayList<>();
		for (GameAgent agent : agents.values()) {
			if (agent.isPlayer()) players.add((PlayerAgent) agent);
			else if (agent.isTeam()) {
				TeamAgent team = (TeamAgent) agent;
                players.addAll(team.getPlayerAgents());
			}
		}
		return players;
	}
	
	public List<TeamAgent> getTeamAgents() {
		List<TeamAgent> teams = new ArrayList<>();
		for (GameAgent agent : agents.values())
			if (agent.isTeam()) teams.add((TeamAgent) agent);
		return teams;
	}

	public List<GameAgent> getAllAgents() {
        return new ArrayList<>(agents.values());
	}

	public List<GameAgent> getAgentsWithScore(int score) {
		List<GameAgent> list = new ArrayList<>();
		for (GameAgent agent : agents.values())
			if (agent.getScore() >= score) list.add(agent);
		return list;
	}
	
	public void resetAllAgents() {
		agents.forEach((id, agent) -> agent.resetAgent());
	}
	
	public void setupAllAgents() {
		agents.forEach((id, agent) -> agent.setupAgent());
	}
	
	public void applyAllAgentRespawnPoints(MinecraftServer server) {
		agents.forEach((id, agent) -> agent.applySpawnPoint(server));
	}
	
	public void tpPlayersToSpawnPosition(MinecraftServer server) {
		agents.forEach((id, agent) -> agent.tpToSpawnPoint(server));
	}
	
	public int getMoneyPerRound() {
		return getIntParam(MONEY_PER_ROUND);
	}
	
	public void giveMoneyToAgents(MinecraftServer server) {
		int totalPlayers = getAllPlayerAgents().size();
		int totalMoney = totalPlayers * getMoneyPerRound();
		int moneyPerTeam = (int)((double)totalMoney / (double)agents.size());
		agents.forEach((id, agent) -> agent.giveMoneyItems(server, moneyPerTeam));
	}
	
	public void spreadPlayers(MinecraftServer server) {
		// TODO 3.7 spread players at start of game option
	}
	
	public void announceWinners(MinecraftServer server) {
		List<GameAgent> winners = getLivingAgents();
		if (winners.size() != 1) return;
		GameAgent winner = winners.get(0);
		winner.onWin(server);
	}

	public void announceScores(MinecraftServer server) {
		chatToAllPlayers(server, UtilMCText.literal("Current Scores:").setStyle(GREEN_BOLD),
				SoundEvents.VILLAGER_CELEBRATE);
		MutableComponent message = UtilMCText.empty().setStyle(GREEN);
		List<GameAgent> agentList = getAllAgents();
		agentList.sort((agent1, agent2) -> (int) (agent2.getScore() - agent1.getScore()));
		for (GameAgent agent : agentList) {
			Component name = agent.getDisplayName(server);
			message.append(name).append(": "+agent.getScore()+"\n");
		}
		chatToAllPlayers(server, message);
	}
	
	public void chatToAllPlayers(MinecraftServer server, Component message, @Nullable SoundEvent sound) {
		for (PlayerAgent agent : getAllPlayerAgents()) {
			ServerPlayer player = agent.getPlayer(server);
			if (player == null) continue;
			player.displayClientMessage(message, false);
			if (sound != null) player.playNotifySound(sound, SoundSource.NEUTRAL, 1, 1);
		}
	}

	public void chatToAllPlayers(MinecraftServer server, Component message) {
		chatToAllPlayers(server, message, null);
	}
	
	public String getDebugInfoString(MinecraftServer server) {
		String info = "type:"+getGameTypeId()
				   +"\nid:"+getInstanceId()
				   +"\nage:"+getAge();
		if (currentPhase != null) info += "\nphase:"+currentPhase.toString();
		return info;
	}
	
	public MutableComponent getDebugInfo(MinecraftServer server) {
		return Component.literal(getDebugInfoString(server));
	}

	public void onPlayerDeath(PlayerAgent player, MinecraftServer server, @Nullable DamageSource source) {
		getCurrentPhase().onPlayerDeath(player, server, source);
	}

	public void onPlayerRespawn(PlayerAgent player, MinecraftServer server) {
		getCurrentPhase().onPlayerRespawn(player, server);
	}

	public void onLogIn(PlayerAgent player, MinecraftServer server) {
		getCurrentPhase().onLogIn(player, server);
	}

	public void onLogOut(PlayerAgent player, MinecraftServer server) {
		getCurrentPhase().onLogOut(player, server);
	}

	public void refillAllAgentKits(MinecraftServer server) {
		agents.forEach((name, agent) -> agent.refillPlayerKit(server));
	}

	public void clearAllPlayerInventories(MinecraftServer server) {
		agents.forEach((name, agent) -> agent.clearPlayerInventory(server));
	}

	public boolean isClearOnStart() {
		return getBooleanParam(CLEAR_ON_START);
	}

	public abstract Component getStartGameMessage(MinecraftServer server);

	public void onGameStart(MinecraftServer server) {
		removeOfflineTeamPlayers(server);
		tpPlayersToSpawnPosition(server);
		if (isClearOnStart()) clearAllPlayerInventories(server);
		refillAllAgentKits(server);
		resetAllPlayerHealth(server);
		chatToAllPlayers(server, getStartGameMessage(server));
		if (requiresSetRespawnPos()) chatTeamSpawns(server);
		forAllPOIs(server, (serv, poi) -> poi.onGameStart(serv));
	}

	public boolean canUseKit(GameAgent agent, String kit) {
		return getCurrentPhase().canAgentUseKit(agent, kit);
	}

	public boolean canOpenShop(GameAgent agent, String shop) {
		return getCurrentPhase().canAgentOpenShop(agent, shop);
	}

	public void resetAllPlayerHealth(MinecraftServer server) {
		for (PlayerAgent agent : getAllPlayerAgents()) {
			ServerPlayer player = agent.getPlayer(server);
			if (player == null) continue;
			player.setHealth(player.getMaxHealth());
			player.getFoodData().setFoodLevel(20);
			player.getFoodData().setSaturation(20);
			player.getFoodData().setExhaustion(0);
		}
	}

	public void onFlagDeath(@NotNull FlagEntity flag, @Nullable DamageSource source) {
		flags.remove(flag);
	}

	public void onFlagSpawn(@NotNull FlagEntity flag) {
		Iterator<FlagEntity> iter = flags.iterator();
		while (iter.hasNext()) {
			FlagEntity f = iter.next();
			if (f.getTeamId().equals(flag.getTeamId())) {
				f.discard();
				iter.remove();
			}
		}
		flags.add(flag);
	}

	public boolean verifyFlagSpawn(FlagEntity entity) {
		return getNumResets() == entity.getGameResetCount();
    }

	public List<FlagEntity> getLivingFlags() {
		List<FlagEntity> list = new ArrayList<>();
		for (FlagEntity flag : flags)
			if (flag.getHealth() > 0.0F) list.add(flag);
 		return list;
	}

	public void awardLivingFlagTeams() {
		Set<String> noRepeats = new HashSet<>();
		getLivingFlags().forEach(flag -> {
			String id = flag.getTeamId();
			if (noRepeats.contains(id)) return;
			GameAgent team = getAgentById(id);
			if (team == null) return;
			team.addScore(1);
			noRepeats.add(id);
		});
	}

	public void awardLivingTeamsTie() {
		double numLiving = currentPhase.getGameData().getLivingAgents().size();
		for (GameAgent agent : currentPhase.getGameData().getLivingAgents())
			agent.addScore(1d/numLiving);
	}

	public void awardLivingTeams() {
		for (GameAgent agent : currentPhase.getGameData().getLivingAgents())
			agent.addScore(1);
	}

	public String[] getAllAgentIds() {
		return agents.keySet().toArray(new String[0]);
	}

	public boolean looseLiveOnDeath(GameAgent gameAgent, MinecraftServer server) {
		return getCurrentPhase().looseLiveOnDeath(gameAgent, server);
	}

	public float getSpawnYaw(PlayerAgent playerAgent) {
		if (playerAgent.getRespawnPoint() == null) return 0;
		for (GameAgent agent : agents.values()) {
			if (playerAgent.isOnSameTeam(agent)) continue;
			if (agent.getRespawnPoint() == null) continue;
			return UtilAngles.getYaw(agent.getRespawnPoint().subtract(playerAgent.getRespawnPoint()));
		}
		return 0;
	}

	public void removeOfflineTeamPlayers(MinecraftServer server) {
		getTeamAgents().forEach(teamAgent -> teamAgent.removeOfflineMembers(server));
	}

	public void chatTeamSpawns(MinecraftServer server) {
		agents.forEach((id, agent) -> agent.chatSpawnPosition(server));
	}

	public boolean allowBlockPlace(PlayerAgent agent, MinecraftServer server, BlockPos pos, Block placedBlock) {
		return true;
	}

	public void forEachFlag(Consumer<FlagEntity> consumer) {
		flags.forEach(consumer);
	}

	public boolean isForceNonMemberSpectator() {
		return getBooleanParam(FORCE_NON_MEMBER_SPEC);
	}

	public void setAllAgentInitialLives(int lives) {
		agents.forEach((id, agent) -> agent.setInitialLives(lives));
	}

	public String[] getAllPlayerAgentNames() {
		List<PlayerAgent> agents = getAllPlayerAgents();
		String[] names = new String[agents.size()];
		for (int i = 0; i < agents.size(); ++i)
			names[i] = agents.get(i).getScoreboardName();
		return names;
	}

	public String[] getAllTeamAgentNames() {
		List<TeamAgent> agents = getTeamAgents();
		String[] names = new String[agents.size()];
		for (int i = 0; i < agents.size(); ++i)
			names[i] = agents.get(i).getId();
		return names;
	}

	public boolean alwaysAllowOpenShop() {
		return getBooleanParam(ALLOW_ALWAYS_SHOP);
	}

	public float getWaterFoodExhaustionRate() {
		return getFloatParam(WATER_FOOD_EXHAUSTION_RATE);
	}

	protected Set<String> getEnabledEvents() {
		return getStringSetParam(EVENTS);
	}

	public void addEvents(String... ids) {
		Collections.addAll(getEnabledEvents(), ids);
	}

	public void removeEvents(String... ids) {
		for (String id : ids) getEnabledEvents().remove(id);
	}

	public String[] getHandleableEvents() {
		return getEnabledEvents().toArray(new String[0]);
	}

    public boolean canHandleEvent(String event) {
		return getEnabledEvents().contains(event);
    }

	/**
	 * @return true if item should be consumed
	 */
	public boolean handleEvent(ServerPlayer player, PlayerAgent agent, String event, CompoundTag params) {
		return MiniGameManager.handleItemEvent(event, player, agent, params);
	}

	public boolean isBuyPhase() {
		return getCurrentPhase().isBuyPhase();
	}

	public boolean isAttackPhase() {
		return getCurrentPhase().isAttackPhase();
	}

	public void putPOI(GamePOI<?> poi) {
		pois.put(poi.getInstanceId(), poi);
	}

	public void removePOI(String instanceId) {
		pois.remove(instanceId);
	}

	public boolean hasPOI(String instanceId) {
		return pois.containsKey(instanceId);
	}

	public boolean hasPOI(Predicate<GamePOI<?>> check) {
		for (GamePOI<?> poi : pois.values())
			if (check.test(poi))
				return true;
		return false;
	}

	public void forPOIsOfType(String typeId, MinecraftServer server,
							  BiConsumer<MinecraftServer, GamePOI<?>> consumer) {
		pois.forEach((id, poi) -> {
			if (poi.getTypeId().equals(typeId))
				consumer.accept(server, poi);
		});
	}

	public void forAllPOIs(MinecraftServer server, BiConsumer<MinecraftServer, GamePOI<?>> consumer) {
		pois.forEach((id, poi) -> consumer.accept(server, poi));
	}

	public List<GamePOI<?>> getPOIs(MinecraftServer server, BiPredicate<MinecraftServer, GamePOI<?>> predicate) {
		List<GamePOI<?>> list = new ArrayList<>();
		for (GamePOI<?> poi : pois.values())
			if (predicate.test(server, poi))
				list.add(poi);
		return list;
	}

	public List<GamePOI<?>> getPOIsOfType(MinecraftServer server, String typeId) {
		return getPOIs(server, (serv, poi) -> poi.getTypeId().equals(typeId));
	}

	public String[] getPOIInstanceIds() {
		return pois.keySet().toArray(new String[0]);
	}

	protected void registerParam(MiniGameParamType<?> type) {
		params.put(type, new MiniGameParamHolder<>(type));
	}

	public boolean usesParam(MiniGameParamType<?> type) {
		return params.containsKey(type);
	}

	public <E> boolean setParam(MiniGameParamType<E> type, E value) {
		MiniGameParamHolder<?,E> holder = (MiniGameParamHolder<?,E>) params.get(type);
		if (holder == null) return false;
		holder.set(value);
		return true;
	}

	/**
	 * use {@link #usesParam(MiniGameParamType)} to check if the param exists.
	 * if it doesn't exist in this game type then the default value is returned.
	 */
	@NotNull
	public <E> E getParam(MiniGameParamType<E> type) {
		MiniGameParamHolder<?,E> holder = (MiniGameParamHolder<?,E>) params.get(type);
		if (holder == null) return type.getDefaultValue();
		return holder.get();
	}

	public boolean getBooleanParam(MiniGameParamType<Boolean> type) {
		return getParam(type);
	}

	public int getIntParam(MiniGameParamType<Integer> type) {
		return getParam(type);
	}

	public double getDoubleParam(MiniGameParamType<Double> type) {
		return getParam(type);
	}

	public float getFloatParam(MiniGameParamType<Float> type) {
		return getParam(type);
	}

	public Vec3 getVec3Param(MiniGameParamType<Vec3> type) {
		return getParam(type);
	}

	public Set<String> getStringSetParam(MiniGameParamType<Set<String>> type) {
		return getParam(type);
	}

	public boolean canAnyPlayerJoin() {
		return getBooleanParam(OPEN_JOINING);
	}

	public boolean canPlayersPickTeams() {
		return getBooleanParam(OPEN_TEAMS) && canAddTeams();
	}

	public boolean canJoinSetupOnly() {
		return getBooleanParam(JOIN_SETUP_ONLY);
	}

	public boolean canPlayerJoinViaGUI() {
		if (!canAnyPlayerJoin()) return false;
		return canJoinSetupOnly() && isSetupPhase();
	}

	protected void registerParams() {
		registerParam(JOIN_SETUP_ONLY);
		registerParam(OPEN_JOINING);
		registerParam(OPEN_TEAMS);
		registerParam(CAN_ADD_PLAYERS);
		registerParam(CAN_ADD_TEAMS);
		registerParam(CLEAR_ON_START);
		registerParam(ALLOW_ALWAYS_SHOP);
		registerParam(FORCE_NON_MEMBER_SPEC);
		registerParam(REQUIRE_SET_SPAWN);
		registerParam(USE_WORLD_BORDER);
		registerParam(DEFAULT_LIVES);
		registerParam(MONEY_PER_ROUND);
		registerParam(WORLD_BORDER_SIZE);
		registerParam(WATER_FOOD_EXHAUSTION_RATE);
		registerParam(GAME_CENTER);
		registerParam(KITS);
		registerParam(SHOPS);
		registerParam(EVENTS);
	}

	public String[] getTeamIds() {
		List<TeamAgent> agents = getTeamAgents();
		String[] ids = new String[agents.size()];
		for (int i = 0; i < ids.length; ++i)
			ids[i] = agents.get(i).getId();
		return ids;
	}
}
