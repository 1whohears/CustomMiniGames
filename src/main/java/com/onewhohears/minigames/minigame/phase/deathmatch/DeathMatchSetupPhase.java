package com.onewhohears.minigames.minigame.phase.deathmatch;

import com.onewhohears.minigames.minigame.data.DeathMatchData;
import com.onewhohears.minigames.minigame.phase.SetupPhase;

import net.minecraft.server.MinecraftServer;

public class DeathMatchSetupPhase<T extends DeathMatchData> extends SetupPhase<T> {

	public DeathMatchSetupPhase(T gameData) {
		this("death_match_setup", gameData);
	}
	
	public DeathMatchSetupPhase(String id, T gameData) {
		super(id, gameData);
		forceAdventureMode = true;
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
	}
	
	@Override
	public void onStop(MinecraftServer server) {
		super.onStop(server);
	}

}
