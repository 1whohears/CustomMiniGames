package com.onewhohears.minigames.minigame.agent;

import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.onewhohears.minigames.data.kits.GameKit;
import com.onewhohears.minigames.data.kits.MiniGameKitsManager;
import com.onewhohears.minigames.init.MiniGameItems;
import com.onewhohears.minigames.minigame.data.MiniGameData;

import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;

public class PlayerAgent extends GameAgent {
	
	private UUID playerId;
	private ServerPlayer player;
	private TeamAgent teamAgent;
	@Nullable private Vec3 deathPosition = null;
	private float deathLookX, deathLookY;
	private String scoreboardName = "";

	public PlayerAgent(String type, String uuid, MiniGameData gameData) {
		super(type, uuid, gameData);
	}
	
	@Override
	public CompoundTag save() {
		CompoundTag nbt = super.save();
		nbt.putString("scoreboardName", scoreboardName);
		return nbt;
	}
	
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		scoreboardName = tag.getString("scoreboardName");
	}
	
	@Override
	public void tickAgent(MinecraftServer server) {
		super.tickAgent(server);
	}
	
	@Override
	protected void tickDead(MinecraftServer server) {
		super.tickDead(server);
	}
	
	@Override
	public void onDeath(MinecraftServer server, @Nullable DamageSource source) {
		super.onDeath(server, source);
		getGameData().onPlayerDeath(this, server, source);
	}

	@Override
	public void teamSyncLives() {
		if (isPlayerOnTeam()) {
			teamAgent.setLives(getLives());
			teamAgent.teamSyncLives();
		}
	}

	public void setDeathPosition(@Nullable Vec3 pos) {
		deathPosition = pos;
	}

	@Nullable
	public Vec3 getDeathPosition() {
		return deathPosition;
	}

	public void setDeathLookDirection(float deathLookX, float deathLookY) {
		this.deathLookX = deathLookX;
		this.deathLookY = deathLookY;
	}

	@Override
	public void onRespawn(MinecraftServer server) {
		super.onRespawn(server);
		getGameData().onPlayerRespawn(this, server);
		if (isDead()) {
			Vec3 pos = getDeathPosition();
			if (pos == null) return;
			ServerPlayer sp = getPlayer(server);
			if (sp == null) return;
			sp.connection.teleport(pos.x(), pos.y(), pos.z(), deathLookY, deathLookX);
		}
	}

	@Override
	public void onLogIn(MinecraftServer server) {
		super.onLogIn(server);
		getGameData().onLogIn(this, server);
	}

	@Override
	public void onLogOut(MinecraftServer server) {
		super.onLogOut(server);
		getGameData().onLogOut(this, server);
	}

	@Override
	public void giveMoneyItems(MinecraftServer server, int amount) {
		ItemStack money = MiniGameItems.MONEY.get().getDefaultInstance();
		money.setCount(amount);
		ServerPlayer sp = getPlayer(server);
		if (sp == null) return;
		sp.addItem(money.copy());
	}

	@Override
	public boolean isOnSameTeam(GameAgent agent) {
		return getId().equals(agent.getId());
	}

	@Override
	public boolean canTickAgent(MinecraftServer server) {
		return getPlayer(server) != null && player.isAddedToWorld();
	}
	
	@Nullable
	public ServerPlayer getPlayer(MinecraftServer server) {
		if (player != null && player.isAddedToWorld()) return player;
		UUID uuid = getPlayerId();
		if (uuid == null) return null;
		player = server.getPlayerList().getPlayer(uuid);
		if (player != null) scoreboardName = player.getScoreboardName();
		return player;
	}
	
	@Nullable
	public UUID getPlayerId() {
		if (playerId == null) playerId = UUID.fromString(getId());
		return playerId;
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	@Override
	public boolean isTeam() {
		return false;
	}

	@Override
	public String getAgentOrTeamId() {
		if (teamAgent != null) return teamAgent.getId();
		return getId();
	}

	@Override
	public boolean isPlayerOnTeam() {
		return teamAgent != null;
	}
	
	@Nullable
	public TeamAgent getTeamAgent() {
		return teamAgent;
	}
	
	public void setTeamAgent(TeamAgent teamAgent) {
		this.teamAgent = teamAgent;
	}
	
	@Override
	public boolean canUseKit(String kit) {
		if (isPlayerOnTeam()) return getTeamAgent().canUseKit(kit);
		return super.canUseKit(kit);
	}
	
	@Override
	public boolean canOpenShop(MinecraftServer server, String shop) {
		if (teamAgent != null) return teamAgent.canOpenShop(server, shop);
		return super.canOpenShop(server, shop);
	}

	@Override
	public void applySpawnPoint(MinecraftServer server) {
		if (!hasRespawnPoint()) return;
		ServerPlayer player = getPlayer(server);
		if (player == null) return;
		player.setRespawnPosition(server.overworld().dimension(), 
				new BlockPos(getRespawnPoint()), 
				0, true, true);
	}

	@Override
	public void tpToSpawnPoint(MinecraftServer server) {
		ServerPlayer player = getPlayer(server);
		if (player == null) return;
		BlockPos pos = player.getRespawnPosition();
		if (pos == null) pos = server.overworld().getSharedSpawnPos();
		ResourceKey<Level> dim = player.getRespawnDimension();
		ServerLevel level = server.getLevel(dim);
		float yaw = getGameData().getSpawnYaw(this);
		player.teleportTo(level, pos.getX(), pos.getY(), pos.getZ(), yaw, 0);
	}

	@Override
	public void onWin(MinecraftServer server) {
		ServerPlayer player = getPlayer(server);
		if (player == null) return;
		Style style = player.getDisplayName().getStyle().withColor(ChatFormatting.LIGHT_PURPLE)
				.withBold(true).withUnderlined(true);
		Component message = Component.empty().append(player.getDisplayName())
				.append(" is the winner!").setStyle(style);
		getGameData().chatToAllPlayers(server, message, SoundEvents.FIREWORK_ROCKET_LAUNCH);
	}

	@Override
	public Component getDebugInfo(MinecraftServer server) {
		MutableComponent message = Component.literal("[");
		ServerPlayer sp = getPlayer(server);
		if (sp == null) message.append(getId());
		else message.append(sp.getDisplayName());
		message.append(",L:"+getLives()+",S:"+getScore()+",M:"+getMoney()+",A:"+getAge()+"]");
		return message;
	}

	@Override
	public Component getDisplayName(MinecraftServer server) {
		ServerPlayer sp = getPlayer(server);
		if (sp == null) return UtilMCText.literal(getId());
		return sp.getDisplayName();
	}

	@Override
	public void refillPlayerKit(MinecraftServer server) {
		String kitName = getSelectedKit();
		if (kitName.isEmpty()) return;
		GameKit kit = MiniGameKitsManager.get().get(kitName);
		if (kit == null) return;
		ServerPlayer player = getPlayer(server);
		if (player == null) return;
		kit.giveItemsRefill(player);
	}

	@Override
	public void clearPlayerInventory(MinecraftServer server) {
		ServerPlayer player = getPlayer(server);
		if (player == null) return;
		player.getInventory().clearContent();
	}

	@Override
	public void resetAgent() {
		super.resetAgent();
		deathPosition = null;
	}

	public boolean allowBlockPlace(MinecraftServer server, BlockPos pos, Block placedBlock) {
		return getGameData().getCurrentPhase().allowBlockPlace(this, server, pos, placedBlock);
	}

	public void consumeForPlayer(MinecraftServer server, Consumer<ServerPlayer> consumer) {
		ServerPlayer player = getPlayer(server);
		if (player != null) consumer.accept(player);
	}

	@Override
	public int getInitialLives() {
		if (getTeamAgent() != null) return getTeamAgent().getInitialLives();
		return super.getInitialLives();
	}

	public String getScoreboardName() {
		return scoreboardName;
	}

	@Override
	public boolean isShareLives() {
		return teamAgent != null && teamAgent.isShareLives();
	}

	@Override
	public int getColor(MinecraftServer server) {
		if (teamAgent != null) return teamAgent.getColor(server);
		return 0xffffff;
	}

	@Override @Nullable
	public PlayerTeam getPlayerTeamForDisplay(MinecraftServer server) {
		if (teamAgent != null) return teamAgent.getPlayerTeamForDisplay(server);
		return super.getPlayerTeamForDisplay(server);
	}

	@Override
	public Vec3 getCurrentPos(MinecraftServer server) {
		ServerPlayer player = getPlayer(server);
		if (player == null) return Vec3.ZERO;
		return player.position();
	}
}
