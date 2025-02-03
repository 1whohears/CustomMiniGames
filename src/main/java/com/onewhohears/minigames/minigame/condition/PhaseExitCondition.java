package com.onewhohears.minigames.minigame.condition;

import com.mojang.logging.LogUtils;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.minigames.minigame.phase.GamePhase;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

public abstract class PhaseExitCondition<D extends MiniGameData> {

	private static final Logger LOGGER = LogUtils.getLogger();

	private final String id;
	private final String nextPhaseId;
	
	protected PhaseExitCondition(String id, String nextPhaseId) {
		this.id = id;
		this.nextPhaseId = nextPhaseId;
	}
	
	public abstract boolean shouldExit(MinecraftServer server, GamePhase<D> currentPhase);
	
	public final void onExit(MinecraftServer server, GamePhase<D> currentPhase) {
        LOGGER.debug("EXIT COND {} to phase {}", id, nextPhaseId);
		currentPhase.onStop(server);
		onLeave(server, currentPhase);
	}

	protected void onLeave(MinecraftServer server, GamePhase<D> currentPhase) {
	}
	
	public String getId() {
		return id;
	}
	
	public String getNextPhaseId() {
		return nextPhaseId;
	}
	
}
