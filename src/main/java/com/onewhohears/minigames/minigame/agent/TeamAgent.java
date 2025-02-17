package com.onewhohears.minigames.minigame.agent;

import java.util.*;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.onewhohears.minigames.minigame.data.MiniGameData;

import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.scores.PlayerTeam;

public class TeamAgent extends GameAgent {
	
	private final Map<String, PlayerAgent> playerAgents = new HashMap<>();
	
	public TeamAgent(String teamName, MiniGameData gameData) {
		super(teamName, gameData);
	}
	
	@Override
	public CompoundTag save() {
		CompoundTag nbt = super.save();
		savePlayers(nbt);
		return nbt;
	}
	
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		loadPlayers(tag);
	}
	
	protected void savePlayers(CompoundTag nbt) {
		ListTag playerList = new ListTag();
		playerAgents.forEach((id, agent) -> playerList.add(agent.save()));
		nbt.put("playerList", playerList);
	}
	
	protected void loadPlayers(CompoundTag nbt) {
		ListTag playerList = nbt.getList("playerList", 10);
		for (int i = 0; i < playerList.size(); ++i) {
			CompoundTag tag = playerList.getCompound(i);
			String id = tag.getString("id");
			PlayerAgent agent = getGameData().createPlayerAgent(id);
			agent.load(tag);
			playerAgents.put(id, agent);
		}
	}
	
	@Override
	public void tickAgent(MinecraftServer server) {
		super.tickAgent(server);
		updatePlayerAgentMap(server);
		tickPlayerAgents(server);
	}
	
	@Override
	public boolean canTickAgent(MinecraftServer server) {
		return getTeam(server) != null;
	}
	
	protected void tickPlayerAgents(MinecraftServer server) {
		playerAgents.forEach((username, player) -> {
			player.tickAgent(server);
			getGameData().getCurrentPhase().tickPlayerAgent(server, player);
		});
	}
	
	protected void updatePlayerAgentMap(MinecraftServer server) {
		PlayerTeam team = getTeam(server);
		if (team == null) return;
		Collection<String> usernames = team.getPlayers();
        playerAgents.entrySet().removeIf(entry ->
				!usernames.contains(entry.getKey()) && entry.getValue().canTickAgent(server));
		for (String username : usernames) {
			if (playerAgents.containsKey(username)) continue;
			ServerPlayer player = server.getPlayerList().getPlayerByName(username);
			if (player != null) {
				PlayerAgent agent = getGameData().createPlayerAgent(player);
				agent.setTeamAgent(this);
				playerAgents.put(username, agent);
			}
		}
	}

	public void removeOfflineMembers(MinecraftServer server) {
		playerAgents.entrySet().removeIf(entry -> !entry.getValue().canTickAgent(server));
	}
	
	@Nullable
	public PlayerTeam getTeam(MinecraftServer server) {
		Collection<PlayerTeam> teams = server.getScoreboard().getPlayerTeams();
		for (PlayerTeam team : teams) 
			if (team.getName().equals(getId())) 
				return team;
		return null;
	}
	
	@Nullable
	public PlayerAgent getPlayerAgentByUsername(String name) {
		return playerAgents.get(name);
	}
	
	@Nullable
	public PlayerAgent getPlayerAgentByUUID(String uuid) {
		for (PlayerAgent agent : getPlayerAgents())
			if (agent.getId().equals(uuid)) 
				return agent;
		return null;
	}
	
	public Collection<PlayerAgent> getPlayerAgents() {
		return playerAgents.values();
	}
	
	public List<PlayerAgent> getLivingPlayerAgents() {
		List<PlayerAgent> living = new ArrayList<>();
		for (PlayerAgent agent : getPlayerAgents())
			if (!agent.isDead()) living.add(agent);
		return living;
	}
	
	@Override
	public void resetAgent() {
		super.resetAgent();
		playerAgents.forEach((username, player) -> player.resetAgent());
	}
	
	@Override
	public void setupAgent() {
		super.setupAgent();
		playerAgents.forEach((username, player) -> player.setupAgent());
	}
	
	@Override
	public boolean isDead() {
		return getLivingPlayerAgents().isEmpty();
	}

	@Override
	public boolean isPlayer() {
		return false;
	}
	
	@Override
	public boolean isPlayerOnTeam() {
		return false;
	}

	@Override
	public boolean isTeam() {
		return true;
	}

	@Override
	public void applySpawnPoint(MinecraftServer server) {
		if (!hasRespawnPoint()) return;
		for (PlayerAgent agent : getPlayerAgents()) {
			agent.setRespawnPoint(getRespawnPoint());
			agent.applySpawnPoint(server);
		}
	}

	@Override
	public void tpToSpawnPoint(MinecraftServer server) {
		for (PlayerAgent agent : getPlayerAgents())
			agent.tpToSpawnPoint(server);
	}
	
	@Override
	public void refillPlayerKit(MinecraftServer server) {
		playerAgents.forEach((username, player) -> player.refillPlayerKit(server));
	}

	@Override
	public void clearPlayerInventory(MinecraftServer server) {
		playerAgents.forEach((username, player) -> player.clearPlayerInventory(server));
	}

	@Override
	public void onWin(MinecraftServer server) {
		PlayerTeam team = getTeam(server);
		if (team == null) return;
		Style style = team.getDisplayName().getStyle().withBold(true).withUnderlined(true);
		Component message = Component.empty().append(team.getFormattedDisplayName())
				.append(" is the winning team!").setStyle(style);
		getGameData().chatToAllPlayers(server, message, SoundEvents.FIREWORK_ROCKET_LAUNCH);
	}

	@Override
	public Component getDebugInfo(MinecraftServer server) {
		MutableComponent message = Component.literal("[");
		PlayerTeam pt = getTeam(server);
		if (pt == null) message.append(getId());
		else message.append(pt.getDisplayName());
		message.append("]");
		return message;
	}

	@Override
	public Component getDisplayName(MinecraftServer server) {
		PlayerTeam pt = getTeam(server);
		if (pt == null) return UtilMCText.literal(getId());
		return pt.getDisplayName();
	}

	@Override
	public void giveMoneyItems(MinecraftServer server, int amount) {
		Collection<PlayerAgent> players = getPlayerAgents();
		int moneyPerPlayer = (int)(amount / (double)players.size());
		players.forEach(player -> player.giveMoneyItems(server, moneyPerPlayer));
	}

	@Override
	public boolean isOnSameTeam(GameAgent agent) {
		if (getId().equals(agent.getId())) return true;
		for (PlayerAgent player : getPlayerAgents())
			if (player.isOnSameTeam(agent))
				return true;
		return false;
	}

	@Override
	public void consumeForPlayer(MinecraftServer server, Consumer<ServerPlayer> consumer) {
		getPlayerAgents().forEach(agent -> agent.consumeForPlayer(server, consumer));
	}

	@Override
	public void setInitialLives(int lives) {
		super.setInitialLives(lives);
		getPlayerAgents().forEach(agent -> agent.setInitialLives(lives));
	}
}
