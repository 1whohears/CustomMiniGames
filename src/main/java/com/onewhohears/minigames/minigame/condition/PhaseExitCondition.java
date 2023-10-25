package com.onewhohears.minigames.minigame.condition;

import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.minigames.minigame.phase.GamePhase;

import net.minecraft.server.MinecraftServer;

public abstract class PhaseExitCondition<D extends MiniGameData> {
	
	private final String id;
	private final String nextPhaseId;
	
	protected PhaseExitCondition(String id, String nextPhaseId) {
		this.id = id;
		this.nextPhaseId = nextPhaseId;
	}
	
	public abstract boolean shouldExit(MinecraftServer server, GamePhase<D> currentPhase);
	
	public void onExit(MinecraftServer server, GamePhase<D> currentPhase) {
		System.out.println("EXIT COND "+id+" to phase "+nextPhaseId);
		currentPhase.onStop(server);
	}
	
	public String getId() {
		return id;
	}
	
	public String getNextPhaseId() {
		return nextPhaseId;
	}
	
}
