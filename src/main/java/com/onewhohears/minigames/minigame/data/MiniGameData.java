package com.onewhohears.minigames.minigame.data;

import java.util.*;

import javax.annotation.Nullable;

import com.onewhohears.minigames.entity.FlagEntity;
import com.onewhohears.onewholibs.util.UtilMCText;
import com.onewhohears.onewholibs.util.math.UtilAngles;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.onewhohears.minigames.init.MiniGameItems;
import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.agent.TeamAgent;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import com.onewhohears.minigames.minigame.phase.SetupPhase;
import com.onewhohears.onewholibs.util.UtilParse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

public abstract class MiniGameData {

	public static final Style GOLD_BOLD = Style.EMPTY.withBold(true)
			.withUnderlined(true).withColor(ChatFormatting.GOLD);
	public static final Style AQUA = Style.EMPTY.withColor(ChatFormatting.AQUA);
	public static final Style GREEN_BOLD = Style.EMPTY.withBold(true)
			.withUnderlined(true).withColor(ChatFormatting.DARK_GREEN);
	public static final Style GREEN = Style.EMPTY.withColor(ChatFormatting.GREEN);

	protected static final Logger LOGGER = LogUtils.getLogger();
	
	private final String gameTypeId;
	private final String instanceId;
	private final Map<String, GameAgent> agents = new HashMap<>();
	private final Map<String, GamePhase<?>> phases = new HashMap<>();
	private final Set<FlagEntity> flags = new HashSet<>();
	private final Set<String> kits = new HashSet<>();
	private final Set<String> shops = new HashSet<>();
	private SetupPhase<?> setupPhase;
	private GamePhase<?> nextPhase;
	private GamePhase<?> currentPhase;
	private int age, resets;
	private boolean isStarted, isStopped, firstTick = true;
	
	protected boolean canAddIndividualPlayers;
	protected boolean canAddTeams;

	protected boolean clearOnStart;
	protected boolean requiresSetRespawnPos, worldBorderDuringGame;
	protected int initialLives = 3, moneyPerRound = 10;
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
		nbt.putInt("resets", resets);
		nbt.putBoolean("isStarted", isStarted);
		nbt.putBoolean("isStopped", isStopped);
		nbt.putBoolean("canAddIndividualPlayers", canAddIndividualPlayers);
		nbt.putBoolean("canAddTeams", canAddTeams);
		nbt.putBoolean("requiresSetRespawnPos", requiresSetRespawnPos);
		nbt.putBoolean("worldBorderDuringGame", worldBorderDuringGame);
		nbt.putInt("initialLives", initialLives);
		nbt.putDouble("gameBorderSize", gameBorderSize);
		nbt.putBoolean("clearOnStart", clearOnStart);
		UtilParse.writeVec3(nbt, gameCenter, "gameCenter");
		saveAgents(nbt);
		savePhases(nbt);
		UtilParse.writeStrings(nbt, "kits", kits);
		UtilParse.writeStrings(nbt, "shops", shops);
		return nbt;
	}
	
	public void load(CompoundTag nbt) {
		age = nbt.getInt("age");
		resets = nbt.getInt("resets");
		isStarted = nbt.getBoolean("isStarted");
		isStopped = nbt.getBoolean("isStopped");
		canAddIndividualPlayers = nbt.getBoolean("canAddIndividualPlayers");
		canAddTeams = nbt.getBoolean("canAddTeams");
		requiresSetRespawnPos = nbt.getBoolean("requiresSetRespawnPos");
		worldBorderDuringGame = nbt.getBoolean("worldBorderDuringGame");
		initialLives = nbt.getInt("initialLives");
		gameBorderSize = nbt.getDouble("gameBorderSize");
		gameCenter = UtilParse.readVec3(nbt, "gameCenter");
		clearOnStart = nbt.getBoolean("clearOnStart");
		loadAgents(nbt);
		loadPhases(nbt);
		kits.clear(); shops.clear();
		kits.addAll(UtilParse.readStringSet(nbt, "kits"));
		shops.addAll(UtilParse.readStringSet(nbt, "shops"));
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
			GameAgent agent;
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
		if (other != null) return null;
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
		isStopped = false;
		changePhase(server, setupPhase.getId());
	}
	
	public void stop(MinecraftServer server) {
        LOGGER.debug("GAME STOP {}", instanceId);
		isStarted = true;
		isStopped = true;
		getCurrentPhase().onStop(server);
	}
	
	public boolean shouldTickGame(MinecraftServer server) {
		return isStarted() && !isStopped();
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
		getCurrentPhase().updateWorldBorder(server);
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
		worldBorderDuringGame = use;
	}
	
	public boolean areAgentRespawnPosSet() {
		if (!requiresSetRespawnPos()) return true;
		for (GameAgent agent : agents.values())
			if (!agent.hasRespawnPoint()) 
				return false;
		return true;
	}
	
	public void addKits(String... ids) {
        kits.addAll(Arrays.asList(ids));
	}
	
	public void removeKit(String id) {
		kits.remove(id);
	}
	
	public String[] getEnabledKitIds() {
		return kits.toArray(new String[0]);
	}

	public boolean hasKit(String id) {
		return kits.contains(id);
	}

	public void addShops(String... ids) {
        Collections.addAll(shops, ids);
	}
	
	public void removeShops(String... ids) {
		for (String id : ids) shops.remove(id);
	}
	
	public String[] getEnabledShopIds() {
		return shops.toArray(new String[0]);
	}

	public boolean hasShop(String id) {
		return shops.contains(id);
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

	public PlayerAgent createPlayerAgent(String uuid) {
		return new PlayerAgent(uuid, this);
	}
	
	public PlayerAgent createPlayerAgent(ServerPlayer player) {
		return createPlayerAgent(player.getStringUUID());
	}

	public TeamAgent createTeamAgent(String teamName) {
		return new TeamAgent(teamName, this);
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
		return moneyPerRound;
	}

	public void setMoneyPerRound(int money) {
		moneyPerRound = money;
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
		return clearOnStart;
	}

	public void setClearOnStart(boolean clear) {
		clearOnStart = clear;
	}

	public abstract Component getStartGameMessage(MinecraftServer server);

	public void onGameStart(MinecraftServer server) {
		tpPlayersToSpawnPosition(server);
		if (isClearOnStart()) clearAllPlayerInventories(server);
		refillAllAgentKits(server);
		resetAllPlayerHealth(server);
		chatToAllPlayers(server, getStartGameMessage(server));
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
}
