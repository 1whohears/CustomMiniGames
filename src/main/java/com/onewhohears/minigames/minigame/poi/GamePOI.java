package com.onewhohears.minigames.minigame.poi;

import com.onewhohears.minigames.MiniGamesMod;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.onewholibs.util.UtilMCText;
import com.onewhohears.onewholibs.util.UtilParse;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static com.onewhohears.minigames.minigame.agent.GameAgent.YELLOW;

public abstract class GamePOI<G extends MiniGameData> {

    @NotNull private final String typeId, instanceId;
    @NotNull private final G gameData;

    @NotNull private Vec3 pos = Vec3.ZERO;
    @NotNull private ResourceKey<Level> dimension = Level.OVERWORLD;

    public GamePOI(@NotNull String typeId, @NotNull String instanceId, @NotNull G gameData) {
        this.typeId = typeId;
        this.instanceId = instanceId;
        this.gameData = gameData;
        this.pos = gameData.getGameCenter();
    }

    public void onGameStart(MinecraftServer server) {
        chatPosition(server);
    }
    public abstract void onRoundStart(MinecraftServer server);

    public boolean canTick(MinecraftServer server) {
        return true;
    }
    public abstract void tick(MinecraftServer server);

    public void load(CompoundTag nbt) {
        pos = UtilParse.readVec3(nbt, "pos");
        if (nbt.contains("dimension")) {
            ResourceLocation dimLoc = new ResourceLocation(nbt.getString("dimension"));
            dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, dimLoc);
        } else dimension = Level.OVERWORLD;
    }

    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("typeId", typeId);
        nbt.putString("instanceId", instanceId);
        UtilParse.writeVec3(nbt, pos, "pos");
        nbt.putString("dimension", dimension.location().toString());
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

    public double distanceToSqr(Vec3 pos) {
        return getPos().distanceToSqr(pos);
    }

    public @NotNull ResourceKey<Level> getDimension() {
        return dimension;
    }

    public void setDimension(@NotNull ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public void chatPosition(MinecraftServer server) {
        Vec3 pos = getPos();
        Component name = UtilMCText.literal(getInstanceId());
        Component nameMessage = UtilMCText.empty().append(name).append(" is at " +
                "x:"+(int)pos.x+",y:"+(int)pos.y+",z:"+(int)pos.z).setStyle(YELLOW);
        getGameData().chatToAllPlayers(server, nameMessage);
        if (MiniGamesMod.isXaeroMinimapLoaded()) {
            String dim = getDimension().location().getPath();
            int color = Mth.randomBetweenInclusive(server.overworld().getRandom(), 0, 15);
            Component waypoint = UtilMCText.literal("xaero-waypoint:"+name.getString()+":"+
                    name.getString().charAt(0)+":"+(int)pos.x+":"+(int)pos.y+":"+(int)pos.z+":"+color+
                    ":false:0:Internal-"+dim+"-waypoints");
            getGameData().chatToAllPlayers(server, waypoint);
        }
    }
}
