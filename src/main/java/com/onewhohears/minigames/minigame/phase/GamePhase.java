package com.onewhohears.minigames.minigame.phase;

import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.agent.TeamAgent;
import com.onewhohears.minigames.minigame.condition.PhaseExitCondition;
import com.onewhohears.minigames.minigame.data.MiniGameData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;

public abstract class GamePhase<T extends MiniGameData> {
	
	private final String id;
	private final T gameData;
	private final PhaseExitCondition<T>[] exitConditions;
	private int age;
	
	@SafeVarargs
	protected GamePhase(String id, T gameData, PhaseExitCondition<T>...exitConditions) {
		this.id = id;
		this.gameData = gameData;
		this.exitConditions = exitConditions;
	}
	
	public CompoundTag save() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("id", id);
		nbt.putInt("age", age);
		return nbt;
	}
	
	public void load(CompoundTag tag) {
		age = tag.getInt("age");
	}
	
	public void tickPhase(MinecraftServer server) {
		++age;
		checkExitConditions(server);
	}
	
	public void tickPlayerAgent(MinecraftServer server, PlayerAgent<?> agent) {
		ServerPlayer player = agent.getPlayer(server);
		if (player == null) return;
		if (!player.gameMode.isCreative()) {
			if (isForceSurvivalMode()) player.setGameMode(GameType.SURVIVAL);
			else if (isForceAdventureMode()) player.setGameMode(GameType.ADVENTURE);
		}
	}
	
	public void tickTeamAgent(MinecraftServer server, TeamAgent<?> agent) {
		
	}
	
	public void onReset(MinecraftServer server) {
		System.out.println("PHASE RESET "+id);
		age = 0;
	}
	
	public void onStart(MinecraftServer server) {
		System.out.println("PHASE START "+id);
		updateWorldBorder(server);
	}
	
	public void onStop(MinecraftServer server) {
		System.out.println("PHASE STOP "+id);
	}
	
	public void checkExitConditions(MinecraftServer server) {
		if (exitConditions == null || exitConditions.length == 0) return;
		for (PhaseExitCondition<T> con : exitConditions) {
			if (con.shouldExit(server, this)) {
				con.onExit(server, this);
				gameData.changePhase(server, con.getNextPhaseId());
				break;
			}
		}
	}
	
	public void updateWorldBorder(MinecraftServer server) {
		WorldBorder border = server.overworld().getWorldBorder();
		if (hasWorldBorder()) {
			double size = getWorldBorderSize();
			double endSize = getWorldBorderEndSize();
			long time = getWorldBorderChangeTime();
			if (size != -1 && time != -1 && endSize != -1) {
				border.setSize(size);
				border.lerpSizeBetween(endSize, size, time);
			} else if (size != -1 && time != -1) {
				border.lerpSizeBetween(border.getSize(), size, time);
			} else if (size != -1) {
				border.setSize(size);
			}
			Vec3 center = getGameData().getGameCenter();
			border.setCenter(center.x, center.z);
		} else border.applySettings(WorldBorder.DEFAULT_SETTINGS);
	}
	
	public String getId() {
		return id;
	}
	
	public T getGameData() {
		return gameData;
	}
	
	public PhaseExitCondition<T>[] getExitConditions() {
		return exitConditions;
	}
	
	public int getAge() {
		return age;
	}
	
	public boolean isSetupPhase() {
		return false;
	}
	
	public boolean isForceAdventureMode() {
		return false;
	}
	
	public boolean isForceSurvivalMode() {
		return false;
	}
	
	public boolean hasWorldBorder() {
		return false;
	}
	
	public double getWorldBorderEndSize() {
		return -1;
	}
	
	public double getWorldBorderSize() {
		return -1;
	}
	
	public long getWorldBorderChangeTime() {
		return -1;
	}
	
	@Override
	public String toString() {
		return "id:"+getId()+",age:"+getAge();
	}
	
}
