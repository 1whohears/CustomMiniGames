package com.onewhohears.minigames.minigame.phase.deathmatch;

import com.onewhohears.minigames.minigame.condition.NeverExitCondition;
import com.onewhohears.minigames.minigame.data.DeathMatchData;
import com.onewhohears.minigames.minigame.phase.GamePhase;

import net.minecraft.server.MinecraftServer;

public class DeathMatchEndPhase<T extends DeathMatchData> extends GamePhase<T> {
	
	public DeathMatchEndPhase(T gameData) {
		super("death_match_end", gameData, new NeverExitCondition<>());
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
		getGameData().announceWinners(server);
	}
	
	@Override
	public void onStop(MinecraftServer server) {
		super.onStop(server);
	}
	
	@Override
	public boolean shouldEndGame() {
		return getAge() >= 200;
	}

}
