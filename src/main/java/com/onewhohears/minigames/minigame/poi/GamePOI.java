package com.onewhohears.minigames.minigame.poi;

import com.onewhohears.minigames.minigame.data.MiniGameData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public abstract class GamePOI<G extends MiniGameData> {

    @NotNull private final String typeId, instanceId;
    @NotNull private final G gameData;

    public GamePOI(@NotNull String typeId, @NotNull String instanceId, @NotNull G gameData) {
        this.typeId = typeId;
        this.instanceId = instanceId;
        this.gameData = gameData;
    }

    public void onGameStart(MinecraftServer server) {

    }

    public void onRoundStart(MinecraftServer server) {

    }

    public boolean canTick(MinecraftServer server) {
        return true;
    }
    public abstract void tick(MinecraftServer server);

    public void load(CompoundTag nbt) {

    }

    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("typeId", typeId);
        nbt.putString("instanceId", instanceId);
        return nbt;
    }

    public @NotNull String getTypeId() {
        return typeId;
    }

    public @NotNull String getInstanceId() {
        return instanceId;
    }

    @NotNull
    public G getGameData() {
        return gameData;
    }
}
