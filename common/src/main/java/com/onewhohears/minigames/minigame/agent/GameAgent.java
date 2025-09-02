package com.onewhohears.minigames.minigame.agent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.logging.LogUtils;
import com.onewhohears.minigames.MiniGamesMod;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.onewholibs.util.UtilMCText;
import com.onewhohears.onewholibs.util.UtilParse;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.function.Consumer;

public abstract class GameAgent {

	public static final Style YELLOW = Style.EMPTY.withColor(ChatFormatting.YELLOW);

	protected static final Logger LOGGER = LogUtils.getLogger();

	private final String id, type;
	private final MiniGameData gameData;
	private int age, deadTicks;
	private double score;
	private int lives, initialLives;
	private int money;
	@Nullable Vec3 respawnPoint = null;
	@NotNull private String selectedKit = "";
	private boolean shareLives = false;
	
	protected GameAgent(String type, String id, MiniGameData gameData) {
		this.type = type;
		this.id = id;
		this.gameData = gameData;
		this.initialLives = getGameData().getDefaultInitialLives();
	}
	
	public CompoundTag save() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("type", type);
		nbt.putString("id", id);
		nbt.putInt("age", age);
		nbt.putInt("deadTicks", deadTicks);
		nbt.putDouble("score", score);
		nbt.putInt("lives", lives);
		nbt.putInt("money", money);
		if (respawnPoint != null)
			UtilParse.writeVec3(nbt, respawnPoint, "respawnPoint");
		nbt.putString("selectedKit", selectedKit);
		nbt.putInt("initialLives", initialLives);
		nbt.putBoolean("shareLives", shareLives);
		return nbt;
	}
	
	public void load(CompoundTag tag) {
		age = tag.getInt("age");
		deadTicks = tag.getInt("deadTicks");
		score = tag.getDouble("score");
		lives = tag.getInt("lives");
		money = tag.getInt("money");
		if (tag.contains("respawnPoint"))
			respawnPoint = UtilParse.readVec3(tag, "respawnPoint");
		selectedKit = tag.getString("selectedKit");
		if (tag.contains("initialLives"))
			initialLives = tag.getInt("initialLives");
		shareLives = tag.getBoolean("shareLives");
	}
	
	public void tickAgent(MinecraftServer server) {
		age++;
		if (isDead()) tickDead(server);
	}
	
	public boolean canTickAgent(MinecraftServer server) {
		return true;
	}
	
	protected void tickDead(MinecraftServer server) {
		deadTicks++;
		if (isPlayer() && !isDead()) tpToSpawnPoint(server);
	}
	
	public void onDeath(MinecraftServer server, @Nullable DamageSource source) {
		if (looseLiveOnDeath(server)) {
			lives = Math.max(lives-1, 0);
			tickDead(server);
			if (isShareLives()) teamSyncLives();
		}
        LOGGER.debug("ON DEATH: {} {}", id, lives);
	}

	public abstract void teamSyncLives();

	public boolean looseLiveOnDeath(MinecraftServer server) {
		return getGameData().looseLiveOnDeath(this, server);
	}

	public void onRespawn(MinecraftServer server) {
		deadTicks = 0;
	}
	
	public boolean shouldRunOnDeath() {
		return !getGameData().isSetupPhase();
	}

	public boolean shouldRunOnRespawn() {
		return !getGameData().isSetupPhase();
	}
	
	public boolean isDead() {
		if (getDeadTicks() <= getGameData().getRespawnTicks()) return true;
		return isOutOfLives();
	}

	public boolean isOutOfLives() {
		return getLives() <= 0;
	}
	
	public void resetAgent() {
		age = 0;
		deadTicks = 0;
		score = 0;
		money = 0;
        LOGGER.debug("RESET AGENT: {} {}", id, lives);
	}
	
	public void setupAgent() {
		lives = getInitialLives();
		addMoney(getGameData().getMoneyPerRound());
        LOGGER.debug("SETUP AGENT: {} {}", id, lives);
	}

	public int getInitialLives() {
		return initialLives;
	}

	public void setInitialLives(int lives) {
		initialLives = lives;
	}

	public String getType() {
		return type;
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

	public int getDeadTicks() {
		return deadTicks;
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
	
	public boolean canOpenShop(MinecraftServer server, String shop) {
		return getGameData().canOpenShop(server, this, shop);
	}
	
	public abstract boolean isPlayer();
	public abstract boolean isPlayerOnTeam();
	public abstract boolean isTeam();
	public abstract String getAgentOrTeamId();
	
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

	public void chatSpawnPosition(MinecraftServer server) {
		Vec3 pos = getRespawnPoint();
		if (pos == null) return;
		Component name = getDisplayName(server);
		Component nameMessage = UtilMCText.empty().append(name).append("'s spawn is at " +
				"x:"+(int)pos.x+",y:"+(int)pos.y+",z:"+(int)pos.z).setStyle(YELLOW);
		getGameData().chatToAllPlayers(server, nameMessage);
		if (MiniGamesMod.isXaeroMinimapLoaded()) {
			int color = Mth.randomBetweenInclusive(server.overworld().getRandom(), 0, 15);
			Component waypoint = UtilMCText.literal("xaero-waypoint:"+name.getString()+"'s Spawn:"+
					name.getString().charAt(0)+":"+(int)pos.x+":"+(int)pos.y+":"+(int)pos.z+":"+color+
					":false:0:Internal-overworld-waypoints");
			getGameData().chatToAllPlayers(server, waypoint);
		}
	}

	public abstract void consumeForPlayer(MinecraftServer server, Consumer<ServerPlayer> consumer);

	public void setShareLives(boolean share) {
		shareLives = share;
	}

	public boolean isShareLives() {
		return shareLives;
	}

	public abstract int getColor(MinecraftServer server);

	@Nullable
	public PlayerTeam getPlayerTeamForDisplay(MinecraftServer server) {
		return null;
	}

	public abstract Vec3 getCurrentPos(MinecraftServer server);
}
