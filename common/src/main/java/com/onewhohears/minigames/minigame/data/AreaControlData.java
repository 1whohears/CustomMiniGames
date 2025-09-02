package com.onewhohears.minigames.minigame.data;

import com.onewhohears.minigames.init.CMGTags;
import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.phase.areacontrol.AreaControlAttackPhase;
import com.onewhohears.minigames.minigame.phase.buyattackrounds.*;
import com.onewhohears.minigames.minigame.poi.AreaControlPOI;
import com.onewhohears.minigames.minigame.poi.GamePOI;
import com.onewhohears.onewholibs.util.UtilMCText;
import com.onewhohears.onewholibs.util.math.UtilGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.onewhohears.minigames.minigame.param.MiniGameParamTypes.*;
import static com.onewhohears.minigames.minigame.poi.AreaControlPOI.CONTROL_SCORE_OBJ_ID;

public class AreaControlData extends BuyAttackData {

    public static AreaControlData createAreaControlMatch(String instanceId, String gameTypeId) {
        AreaControlData game = new AreaControlData(instanceId, gameTypeId);
        game.setPhases(new BuyAttackSetupPhase<>(game),
                new BuyAttackBuyPhase<>(game),
                new AreaControlAttackPhase<>(game),
                new BuyAttackAttackEndPhase<>(game),
                new BuyAttackEndPhase<>(game));
        game.setParam(CAN_ADD_PLAYERS, false);
        game.setParam(CAN_ADD_TEAMS, true);
        game.setParam(REQUIRE_SET_SPAWN, true);
        game.setParam(USE_WORLD_BORDER, false);
        game.setParam(DEFAULT_LIVES, 1000);
        game.setParam(RESPAWN_TICKS, 200);
        game.setParam(ROUNDS_TO_WIN, 10);
        game.getAllowedPoiTypes().add("area_control");
        return game;
    }

    public AreaControlData(String instanceId, String gameTypeId) {
        super(instanceId, gameTypeId);
    }

    @Override
    public void onGameStart(MinecraftServer server) {
        super.onGameStart(server);
        resetScoreboard(server);
    }

    @Override
    public void reset(MinecraftServer server) {
        super.reset(server);
        resetScoreboard(server);
    }

    protected void resetScoreboard(MinecraftServer server) {
        Objective obj = server.getScoreboard().getObjective(CONTROL_SCORE_OBJ_ID);
        if (obj == null) return;
        server.getScoreboard().getPlayerScores(obj).forEach(Score::reset);
        server.getScoreboard().setDisplayObjective(1, null);
    }

    public List<GamePOI<?>> getAreasControlledByAgent(MinecraftServer server, GameAgent agent) {
        return getPOIs(server, (serv, poi) -> poi.getTypeId().equals("area_control")
                && ((AreaControlPOI) poi).getController().equals(agent.getAgentOrTeamId()));
    }

    public int getAgentAreasNum(MinecraftServer server, GameAgent agent) {
        return getAreasControlledByAgent(server, agent).size();
    }

    public int getTotalAreasNum(MinecraftServer server) {
        return getPOIs(server, (serv, poi) -> poi.getTypeId().equals("area_control")).size();
    }

    public void awardByNumControlledAreas(MinecraftServer server) {
        getAllAgents().forEach(agent -> agent.addScore(getAgentAreasNum(server, agent)));
    }

    public boolean allAreasControlledBySame(MinecraftServer server) {
        AtomicBoolean skip = new AtomicBoolean(false);
        AtomicReference<String> agentId = new AtomicReference<>("");
        forPOIsOfType("area_control", server, (serv, poi) -> {
            if (skip.get()) return;
            AreaControlPOI area = (AreaControlPOI) poi;
            if (area.getController().isEmpty()) {
                skip.set(true);
                return;
            }
            if (agentId.get().isEmpty()) {
                agentId.set(area.getController());
            } else if (!agentId.get().equals(area.getController())) {
                skip.set(true);
            }
        });
        return !skip.get();
    }

    @Override
    public boolean allowBlockPlace(PlayerAgent agent, MinecraftServer server, BlockPos pos, Block block) {
        Optional<Holder.Reference<Block>> optional = ForgeRegistries.BLOCKS.getDelegate(block);
        if (optional.isEmpty()) return true;
        Holder.Reference<Block> holder = optional.get();
        int all = getIntParam(BAN_ALL_BLOCKS_RADIUS)^2;
        int white = getIntParam(WHITE_LIST_BLOCKS_RADIUS)^2;
        int black = getIntParam(BLACK_LIST_BLOCKS_RADIUS)^2;
        AtomicBoolean allow = new AtomicBoolean(true);
        Vec3 posV = UtilGeometry.toVec3(pos);
        forPOIsOfType("area_control", server, (serv, poi) -> {
            if (!allow.get()) return;
            double distSqr = poi.distanceToSqr(posV);
            if (distSqr <= all) {
                allow.set(false);
                agent.consumeForPlayer(server, player -> player.sendSystemMessage(UtilMCText.literal(
                        "Cannot place blocks in the NO BLOCKS range!").setStyle(RED)));
                return;
            }
            if (distSqr <= white && !holder.is(CMGTags.Blocks.FLAG_PLACE_WHITE_LIST)) {
                allow.set(false);
                agent.consumeForPlayer(server, player -> player.sendSystemMessage(UtilMCText.literal(
                        "Cannot place this block in the WHITE LIST range!").setStyle(RED)));
                return;
            }
            if (distSqr <= black && holder.is(CMGTags.Blocks.FLAG_PLACE_BLACK_LIST)) {
                allow.set(false);
                agent.consumeForPlayer(server, player -> player.sendSystemMessage(UtilMCText.literal(
                        "Cannot place this block in the BLACK LIST range!").setStyle(RED)));
                return;
            }
        });
        return allow.get();
    }

    public float getAreaControlPointsMax() {
        return getParam(AREA_CONTROL_POINTS_MAX);
    }

    public int getAreaRadius() {
        return getParam(AREA_RADIUS);
    }

    public float getControlPointRate() {
        return getParam(POINTS_PER_PLAYER_PER_SECOND);
    }

    @Override
    protected void registerParams() {
        super.registerParams();
        registerParam(BAN_ALL_BLOCKS_RADIUS);
        registerParam(BLACK_LIST_BLOCKS_RADIUS);
        registerParam(WHITE_LIST_BLOCKS_RADIUS);
        registerParam(AREA_CONTROL_POINTS_MAX);
        registerParam(AREA_RADIUS);
        registerParam(POINTS_PER_PLAYER_PER_SECOND);
    }
}
