package com.onewhohears.minigames.minigame.poi;

import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.onewholibs.util.UtilParse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public abstract class GamePOI<G extends MiniGameData> {

    @NotNull private final String typeId, instanceId;
    @NotNull private final G gameData;

    @NotNull private Vec3 pos = Vec3.ZERO;

    public GamePOI(@NotNull String typeId, @NotNull String instanceId, @NotNull G gameData) {
        this.typeId = typeId;
        this.instanceId = instanceId;
        this.gameData = gameData;
        this.pos = gameData.getGameCenter();
    }

    public abstract void onGameStart(MinecraftServer server);
    public abstract void onRoundStart(MinecraftServer server);

    public boolean canTick(MinecraftServer server) {
        return true;
    }
    public abstract void tick(MinecraftServer server);

    public void load(CompoundTag nbt) {
        pos = UtilParse.readVec3(nbt, "pos");
    }

    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("typeId", typeId);
        nbt.putString("instanceId", instanceId);
        UtilParse.writeVec3(nbt, pos, "pos");
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

    @NotNull
    public Vec3 getPos() {
        return pos;
    }

    public void setPos(@NotNull Vec3 pos) {
        this.pos = pos;
    }
}
