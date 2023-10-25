package com.onewhohears.minigames.minigame.condition;

import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.minigames.minigame.phase.GamePhase;

import net.minecraft.server.MinecraftServer;

public class NeverExitCondition<T extends MiniGameData> extends PhaseExitCondition<T> {
	
	public NeverExitCondition() {
		super("never_exit", "");
	}

	@Override
	public boolean shouldExit(MinecraftServer server, GamePhase<T> currentPhase) {
		return false;
	}

}
