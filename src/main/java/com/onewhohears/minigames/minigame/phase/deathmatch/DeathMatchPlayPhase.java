package com.onewhohears.minigames.minigame.phase.deathmatch;

import com.onewhohears.minigames.minigame.condition.DeathMatchEndCondition;
import com.onewhohears.minigames.minigame.data.DeathMatchData;
import com.onewhohears.minigames.minigame.phase.GamePhase;

import net.minecraft.server.MinecraftServer;

public class DeathMatchPlayPhase<T extends DeathMatchData> extends GamePhase<T> {
	
	public DeathMatchPlayPhase(T gameData) {
		super("death_match_play", gameData, new DeathMatchEndCondition<>());
	}
	
	@Override
	public void tickPhase(MinecraftServer server) {
		super.tickPhase(server);
	}
	
	@Override
	public void onReset(MinecraftServer server) {
		super.onReset(server);
	}
	
	@Override
	public void onStart(MinecraftServer server) {
		super.onStart(server);
		getGameData().tpPlayersToSpawnPosition(server);
	}
	
	@Override
	public void onStop(MinecraftServer server) {
		super.onStop(server);
	}
	
	@Override
	public boolean isForceSurvivalMode() {
		return true;
	}
	
	@Override
	public boolean hasWorldBorder() {
		return getGameData().useWorldBorderDuringGame();
	}
	
	@Override
	public double getWorldBorderSize() {
		return getGameData().getGameBorderSize();
	}

}
