package com.onewhohears.minigames.minigame.phase;

import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.condition.NeverExitCondition;
import com.onewhohears.minigames.minigame.data.MiniGameData;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;

public abstract class SetupPhase<T extends MiniGameData> extends GamePhase<T> {
	
	protected boolean smallWorldBorderStartArea;
	
	protected SetupPhase(String id, T gameData) {
		super(id, gameData, new NeverExitCondition<>());
	}
	
	@Override
	public CompoundTag save() {
		CompoundTag nbt = super.save();
		nbt.putBoolean("smallWorldBorderStartArea", smallWorldBorderStartArea);
		return nbt;
	}
	
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		smallWorldBorderStartArea = tag.getBoolean("smallWorldBorderStartArea");
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
		if (smallWorldBorderStartArea) {
			BlockPos pos = new BlockPos(getGameData().getGameCenter());
			server.overworld().setDefaultSpawnPos(pos, 0);
		}
	}
	
	@Override
	public void onStop(MinecraftServer server) {
		super.onStop(server);
	}
	
	@Override
	public boolean isForceAdventureMode() {
		return getGameData().forceAdventureDuringSetup();
	}
	
	@Override
	public boolean isSetupPhase() {
		return true;
	}
	
	@Override
	public boolean hasWorldBorder() {
		return smallWorldBorderStartArea;
	}
	
	@Override
	public double getWorldBorderSize() {
		return 32;
	}

	public boolean canAgentOpenShop(MinecraftServer server, GameAgent agent, String shop) {
		return false;
	}

}
