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

public abstract class TeamAgent extends GameAgent {
	
	protected final Map<String, PlayerAgent> playerAgents = new HashMap<>();
	
	public TeamAgent(String type, String teamName, MiniGameData gameData) {
		super(type, teamName, gameData);
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
		return teamExists(server);
	}

	public abstract boolean teamExists(MinecraftServer server);

	protected void tickPlayerAgents(MinecraftServer server) {
		playerAgents.forEach((username, player) -> {
			player.tickAgent(server);
			getGameData().getCurrentPhase().tickPlayerAgent(server, player);
		});
	}
	
	protected abstract void updatePlayerAgentMap(MinecraftServer server);

	public void removeOfflineMembers(MinecraftServer server) {
		playerAgents.entrySet().removeIf(entry -> !entry.getValue().canTickAgent(server));
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

	public abstract void onWin(MinecraftServer server);

	public abstract Component getDebugInfo(MinecraftServer server);

	public abstract Component getDisplayName(MinecraftServer server);

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
