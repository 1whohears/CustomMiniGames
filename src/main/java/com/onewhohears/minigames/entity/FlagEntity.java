package com.onewhohears.minigames.entity;

import java.util.Collections;
import java.util.List;

import com.mojang.logging.LogUtils;
import com.onewhohears.minigames.init.MiniGameEntities;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class FlagEntity extends Mob {

	public static boolean spawnFlag(MiniGameData data, GameAgent team, Level level) {
		if (team.getRespawnPoint() == null) return false;
		FlagEntity flag = MiniGameEntities.FLAG.get().create(level);
		if (flag == null) return false;
		flag.setPos(team.getRespawnPoint());
		flag.linkGame(data, team);
		return level.addFreshEntity(flag);
	}

	public static final EntityDataAccessor<String> GAME_INSTANCE_ID = SynchedEntityData.defineId(FlagEntity.class, EntityDataSerializers.STRING);
	public static final EntityDataAccessor<String> TEAM_ID = SynchedEntityData.defineId(FlagEntity.class, EntityDataSerializers.STRING);

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1000000)
				.add(Attributes.MOVEMENT_SPEED, 0);
	}

	public static final List<ItemStack> EMPTY_LIST = Collections.emptyList();

	private static final Logger LOGGER = LogUtils.getLogger();

	private int gameResetCount = 0;

	public FlagEntity(EntityType<? extends FlagEntity> entityType, Level level) {
		super(entityType, level);
		setPersistenceRequired();
		this.blocksBuilding = true;
		this.noPhysics = true;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(GAME_INSTANCE_ID, "");
		entityData.define(TEAM_ID, "");
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		nbt.putString("gameInstanceId", getGameInstanceId());
		nbt.putString("teamId", getTeamId());
		nbt.putInt("gameResetCount", getGameResetCount());
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		setGameInstanceId(nbt.getString("gameInstanceId"));
		setTeamId(nbt.getString("teamId"));
		setGameResetCount(nbt.getInt("gameResetCount"));
	}

	@Override
	public void tick() {
		super.tick();
		if (!getLevel().isClientSide() && tickCount == 1) verifySelf();
	}

	protected void verifySelf() {
		if (getGameInstanceId().isEmpty()) return;
		MiniGameData data = getGameData();
		if (data == null || data.isStopped() || !data.verifyFlagSpawn(this)) {
			discard();
			return;
		}
		data.onFlagSpawn(this);
	}

	public void onDeath(@Nullable DamageSource source) {
		if (getLevel().isClientSide()) return;
		MiniGameData data = getGameData();
		if (data == null) {
			discard();
			return;
		}
		data.onFlagDeath(this, source);
	}

	public void linkGame(MiniGameData data, GameAgent team) {
		setGameInstanceId(data.getInstanceId());
		setTeamId(team.getId());
		setGameResetCount(data.getNumResets());
	}

	@Nullable
	public MiniGameData getGameData() {
		if (getLevel().isClientSide()) return null;
		if (MiniGameManager.get() == null) {
            LOGGER.warn("MiniGameManager is somehow null {}", this);
			return null;
		}
		return MiniGameManager.get().getRunningGame(getGameInstanceId());
	}

	@NotNull
	public String getGameInstanceId() {
		return entityData.get(GAME_INSTANCE_ID);
	}

	@NotNull
	public String getTeamId() {
		return entityData.get(TEAM_ID);
	}

	public void setGameInstanceId(@NotNull String id) {
		entityData.set(GAME_INSTANCE_ID, id);
	}

	public void setTeamId(@NotNull String id) {
		entityData.set(TEAM_ID, id);
	}

	public void setGameResetCount(int resets) {
		gameResetCount = resets;
	}

	public int getGameResetCount() {
		return gameResetCount;
	}

	@Override
	public boolean isAffectedByPotions() {
		return false;
	}

	@Override
	public @NotNull Iterable<ItemStack> getArmorSlots() {
		return EMPTY_LIST;
	}

	@Override
	public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setItemSlot(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
		
	}

	@Override
	public @NotNull HumanoidArm getMainArm() {
		return HumanoidArm.RIGHT;
	}

	@Override
	public boolean isNoAi() {
		return true;
	}

	@Override
	public @NotNull Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public boolean isPersistenceRequired() {
		return true;
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	protected boolean canRide(@NotNull Entity vehicle) {
		return false;
	}
}
