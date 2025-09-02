package com.onewhohears.minigames.minigame.poi;

import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.data.AreaControlData;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AreaControlPOI extends GamePOI<AreaControlData> {

    public static final String CONTROL_SCORE_OBJ_ID = "control_score";

    private float controlScore = 0;
    @NotNull private String controller = "";

    public AreaControlPOI(@NotNull String typeId, @NotNull String instanceId, @NotNull AreaControlData gameData) {
        super(typeId, instanceId, gameData);
    }

    @Override
    public CompoundTag save() {
        CompoundTag nbt = super.save();
        nbt.putFloat("controlScore", controlScore);
        nbt.putString("controller", controller);
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        controlScore = nbt.getFloat("controlScore");
        controller = nbt.getString("controller");
    }

    protected void tickPlayerPoints(MinecraftServer server) {
        if (!getGameData().isAttackPhase() || server.getTickCount() % 20 != 0) return;
        ServerLevel level = server.getLevel(getDimension());
        if (level == null) return;
        int radSqr = getRadius() * getRadius();
        List<ServerPlayer> players = level.getPlayers(player -> !player.isSpectator()
                && player.position().y() >= getPos().y()
                && distanceToSqr(player.position()) <= radSqr);
        if (players.isEmpty()) return;
        float scoreRate = getGameData().getControlPointRate();
        for (ServerPlayer player : players) {
            PlayerAgent agent = getGameData().getPlayerAgentByUUID(player.getStringUUID());
            if (agent == null) continue;
            addControlScore(server, agent, scoreRate);
            if (getControlScore() != 100) player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.NEUTRAL, 1, 1);
        }
    }

    @Override
    public void tick(MinecraftServer server) {
        tickPlayerPoints(server);
    }

    @Override
    public void onGameStart(MinecraftServer server) {
        super.onGameStart(server);
        onRoundStart(server);
    }

    @Override
    public void onRoundStart(MinecraftServer server) {
        setController("");
        setControlScore(0);
        updateScoreboard(server, null);
    }

    public int getRadius() {
        return getGameData().getAreaRadius();
    }

    public float getControlScore() {
        return controlScore;
    }

    public void setControlScore(float controlScore) {
        if (controlScore < 0)
            controlScore = 0;
        else if (controlScore > getGameData().getAreaControlPointsMax())
            controlScore = getGameData().getAreaControlPointsMax();
        this.controlScore = controlScore;
    }

    public void addControlScore(MinecraftServer server, GameAgent agent, float controlScore) {
        if (getController().isEmpty()) {
            setController(agent.getAgentOrTeamId());
            setControlScore(getControlScore() + controlScore);
        } else if (getController().equals(agent.getAgentOrTeamId())) {
            setControlScore(getControlScore() + controlScore);
        } else {
            setControlScore(getControlScore() - controlScore);
            if (getControlScore() == 0) {
                setController("");
                agent = null;
            }
        }
        updateScoreboard(server, agent);
    }

    protected void updateScoreboard(MinecraftServer server, @Nullable GameAgent agent) {
        Objective obj = server.getScoreboard().getObjective(CONTROL_SCORE_OBJ_ID);
        if (obj == null) {
            obj = server.getScoreboard().addObjective(AreaControlPOI.CONTROL_SCORE_OBJ_ID,
                    ObjectiveCriteria.DUMMY, UtilMCText.literal("Area Health"),
                    ObjectiveCriteria.RenderType.INTEGER);
        }
        server.getScoreboard().setDisplayObjective(1, obj);
        Score score = server.getScoreboard().getOrCreatePlayerScore(getInstanceId(), obj);
        score.setScore((int)getControlScore());
        if (agent == null) {
            PlayerTeam team = server.getScoreboard().getPlayersTeam(getInstanceId());
            if (team != null) server.getScoreboard().removePlayerFromTeam(getInstanceId(), team);
            return;
        }
        if (getController().equals(agent.getAgentOrTeamId())) {
            PlayerTeam team = agent.getPlayerTeamForDisplay(server);
            if (team != null) server.getScoreboard().addPlayerToTeam(getInstanceId(), team);
        }
    }

    public @NotNull String getController() {
        return controller;
    }

    public void setController(@NotNull String controller) {
        this.controller = controller;
    }
}
