package com.onewhohears.minigames.minigame.phase.deathmatch;

import com.onewhohears.minigames.minigame.condition.NeverExitCondition;
import com.onewhohears.minigames.minigame.data.DeathMatchData;
import com.onewhohears.minigames.minigame.phase.GamePhase;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class DeathMatchEndPhase<T extends DeathMatchData> extends GamePhase<T> {
	
	public DeathMatchEndPhase(T gameData) {
		this("death_match_end", gameData);
	}
	
	public DeathMatchEndPhase(String id, T gameData) {
		super(id, gameData, new NeverExitCondition<>());
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
		getGameData().getAllPlayerAgents().forEach(agent -> {
			ServerPlayer sp = agent.getPlayer(server);
			if (sp == null) return;
			if (!sp.gameMode.isCreative() && !sp.gameMode.isSurvival())
				sp.setGameMode(GameType.SURVIVAL);
		});
	}
	
	@Override
	public boolean shouldEndGame() {
		return getAge() >= 200;
	}

}
