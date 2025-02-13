package com.onewhohears.minigames.minigame.agent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.logging.LogUtils;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.onewholibs.util.UtilParse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public abstract class GameAgent {

	private static final Logger LOGGER = LogUtils.getLogger();

	private final String id;
	private final MiniGameData gameData;
	private int age;
	private double score;
	private int lives;
	private int money;
	@Nullable Vec3 respawnPoint = null;
	@NotNull private String selectedKit = "";
	
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
		nbt.putDouble("score", score);
		nbt.putInt("lives", lives);
		nbt.putInt("money", money);
		if (respawnPoint != null)
			UtilParse.writeVec3(nbt, respawnPoint, "respawnPoint");
		nbt.putString("selectedKit", selectedKit);
		return nbt;
	}
	
	public void load(CompoundTag tag) {
		age = tag.getInt("age");
		score = tag.getDouble("score");
		lives = tag.getInt("lives");
		money = tag.getInt("money");
		if (tag.contains("respawnPoint"))
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
		if (looseLiveOnDeath(server)) lives = Math.max(lives-1, 0);
        LOGGER.debug("ON DEATH: {} {}", id, lives);
	}

	public boolean looseLiveOnDeath(MinecraftServer server) {
		return getGameData().looseLiveOnDeath(this, server);
	}

	public void onRespawn(MinecraftServer server) {
	}
	
	public boolean shouldRunOnDeath() {
		return !getGameData().isSetupPhase();
	}

	public boolean shouldRunOnRespawn() {
		return !getGameData().isSetupPhase();
	}
	
	public boolean isDead() {
		return getLives() <= 0;
	}
	
	public void resetAgent() {
		age = 0;
		score = 0;
		money = 0;
        LOGGER.debug("RESET AGENT: {} {}", id, lives);
	}
	
	public void setupAgent() {
		lives = getGameData().getInitialLives();
		addMoney(getGameData().getMoneyPerRound());
        LOGGER.debug("SETUP AGENT: {} {}", id, lives);
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
	
	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public void addScore(double score) {
		setScore(getScore() + score);
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

	public void addMoney(int money) {
		this.money += money;
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
	
	public @NotNull String getSelectedKit() {
		if (selectedKit.isEmpty() && getAvailableKits().length > 0)
			selectedKit = getAvailableKits()[0];
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
		return getGameData().canUseKit(this, kit);
	}
	
	public boolean canOpenShop(String shop) {
		return getGameData().canOpenShop(this, shop);
	}
	
	public abstract boolean isPlayer();
	public abstract boolean isPlayerOnTeam();
	public abstract boolean isTeam();
	
	public abstract void applySpawnPoint(MinecraftServer server);
	public abstract void tpToSpawnPoint(MinecraftServer server);
	public abstract void onWin(MinecraftServer server);
	public abstract void refillPlayerKit(MinecraftServer server);
	public abstract void clearPlayerInventory(MinecraftServer server);
	
	public abstract Component getDebugInfo(MinecraftServer server);
	public abstract Component getDisplayName(MinecraftServer server);

	public void onLogIn(MinecraftServer server) {
	}

	public void onLogOut(MinecraftServer server) {
	}

	public abstract void giveMoneyItems(MinecraftServer server, int amount);
	public abstract boolean isOnSameTeam(GameAgent agent);
}
