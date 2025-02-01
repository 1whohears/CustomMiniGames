package com.onewhohears.minigames.minigame.agent;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.onewholibs.util.UtilParse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

public abstract class GameAgent {
	
	private final String id;
	private final MiniGameData gameData;
	private int age;
	private int score;
	private int lives;
	private int money;
	private Vec3 respawnPoint;
	private String selectedKit = "";
	
	protected GameAgent(String id, MiniGameData gameData) {
		this.id = id;
		this.gameData = gameData;
	}
	
	public CompoundTag save() {
		CompoundTag nbt = new CompoundTag();
		nbt.putBoolean("isPlayer", isPlayer());
		nbt.putBoolean("isTeam", isTeam());
		nbt.putString("id", id);
		nbt.putInt("age", age);
		nbt.putInt("score", score);
		nbt.putInt("lives", lives);
		nbt.putInt("money", money);
		UtilParse.writeVec3(nbt, respawnPoint, "respawnPoint");
		nbt.putString("selectedKit", selectedKit);
		return nbt;
	}
	
	public void load(CompoundTag tag) {
		age = tag.getInt("age");
		score = tag.getInt("score");
		lives = tag.getInt("lives");
		money = tag.getInt("money");
		respawnPoint = UtilParse.readVec3(tag, "respawnPoint");
		selectedKit = tag.getString("selectedKit");
	}
	
	public void tickAgent(MinecraftServer server) {
		age++;
		if (isDead()) tickDead(server);
	}
	
	public boolean canTickAgent(MinecraftServer server) {
		return true;
	}
	
	protected void tickDead(MinecraftServer server) {
	}
	
	public void onDeath(MinecraftServer server, @Nullable DamageSource source) {
		lives = Math.max(lives-1, 0);
		System.out.println("ON DEATH: "+id+" "+lives);
	}
	
	public boolean shouldRunOnDeath() {
		return !getGameData().isSetupPhase();
	}
	
	public boolean isDead() {
		return getLives() <= 0;
	}
	
	public void resetAgent() {
		age = 0;
		score = 0;
		money = 0;
		System.out.println("RESET AGENT: "+id+" "+lives);
	}
	
	public void setupAgent() {
		lives = getGameData().getInitialLives();
		System.out.println("SETUP AGENT: "+id+" "+lives);
	}
	
	public String getId() {
		return id;
	}
	
	public MiniGameData getGameData() {
		return gameData;
	}
	
	public int getAge() {
		return age;
	}
	
	public int getScore() {
		return score;
	}
	
	public int getLives() {
		return lives;
	}
	
	public void setLives(int lives) {
		this.lives = lives;
	}
	
	public int getMoney() {
		return money;
	}
	
	public void setMoney(int money) {
		this.money = money;
	}
	
	@Nullable
	public Vec3 getRespawnPoint() {
		return respawnPoint;
	}
	
	public void setRespawnPoint(Vec3 pos) {
		this.respawnPoint = pos;
	}
	
	public boolean hasRespawnPoint() {
		return respawnPoint != null;
	}
	
	public String getSelectedKit() {
		return selectedKit;
	}
	
	public void setSelectedKit(@Nonnull String kit) {
		if (!canUseKit(kit)) return;
		selectedKit = kit;
	}
	
	public String[] getAvailableKits() {
		return getGameData().getEnabledKitIds();
	}
	
	public String[] getAvailableShops() {
		return getGameData().getEnabledShopIds();
	}
	
	public boolean canUseKit(String kit) {
		return Arrays.asList(getAvailableKits()).contains(kit);
	}
	
	public boolean canOpenShop(String shop) {
		return Arrays.asList(getAvailableShops()).contains(shop);
	}
	
	public abstract boolean isPlayer();
	public abstract boolean isPlayerOnTeam();
	public abstract boolean isTeam();
	
	public abstract void applySpawnPoint(MinecraftServer server);
	public abstract void tpToSpawnPoint(MinecraftServer server);
	public abstract void onWin(MinecraftServer server);
	public abstract void refillPlayerKit(MinecraftServer server);
	
	public abstract Component getDebugInfo(MinecraftServer server);
	
}
