package com.onewhohears.minigames.minigame.phase.buyattackrounds;

import com.onewhohears.minigames.minigame.data.BuyAttackData;
import com.onewhohears.minigames.minigame.phase.SetupPhase;
import net.minecraft.server.MinecraftServer;

public class BuyAttackSetupPhase<T extends BuyAttackData> extends SetupPhase<T> {

	public BuyAttackSetupPhase(T gameData) {
		this("buy_attack_setup", gameData);
	}

	public BuyAttackSetupPhase(String id, T gameData) {
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
