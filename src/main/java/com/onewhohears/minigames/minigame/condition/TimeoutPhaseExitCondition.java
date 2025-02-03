package com.onewhohears.minigames.minigame.condition;

import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.minigames.minigame.phase.GamePhase;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class TimeoutPhaseExitCondition<D extends MiniGameData> extends PhaseExitCondition<D> {
	
	public final int time;
	public final String timeEndTranslatable;
	
	public TimeoutPhaseExitCondition(String id, String nextPhaseId, int time, String timeEndTranslatable) {
		super(id, nextPhaseId);
		this.time = time;
		this.timeEndTranslatable = timeEndTranslatable;
	}
	
	public TimeoutPhaseExitCondition(String id, String nextPhaseId, int time) {
		this(id, nextPhaseId, time, null);
	}

	@Override
	public boolean shouldExit(MinecraftServer server, GamePhase<D> currentPhase) {
		return currentPhase.getAge() > time;
	}
	
	@Override
	public void onLeave(MinecraftServer server, GamePhase<D> currentPhase) {
		if (timeEndTranslatable != null)
			currentPhase.getGameData().chatToAllPlayers(server, 
				Component.translatable(timeEndTranslatable));
	}

}
