package com.onewhohears.minigames.minigame.condition;

import com.onewhohears.minigames.minigame.data.DeathMatchData;
import com.onewhohears.minigames.minigame.phase.GamePhase;

import net.minecraft.server.MinecraftServer;

public class DeathMatchEndCondition<D extends DeathMatchData> extends PhaseExitCondition<D> {

	public DeathMatchEndCondition() {
		super("death_match_end", "death_match_end");
	}

	@Override
	public boolean shouldExit(MinecraftServer server, GamePhase<D> currentPhase) {
		return currentPhase.getGameData().getLivingAgents().size() == 1;
	}

}
