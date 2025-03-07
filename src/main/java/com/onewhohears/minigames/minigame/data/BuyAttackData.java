package com.onewhohears.minigames.minigame.data;

import com.onewhohears.minigames.minigame.phase.buyattackrounds.*;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import static com.onewhohears.minigames.minigame.param.MiniGameParamTypes.*;

public class BuyAttackData extends MiniGameData {

    public static BuyAttackData createBuyAttackPhaseMatch(String instanceId, String gameTypeId) {
        BuyAttackData game = new BuyAttackData(instanceId, gameTypeId);
        game.setPhases(new BuyAttackSetupPhase<>(game),
                new BuyAttackBuyPhase<>(game),
                new BuyAttackAttackPhase<>(game),
                new BuyAttackAttackEndPhase<>(game),
                new BuyAttackEndPhase<>(game));
        game.setParam(CAN_ADD_PLAYERS, true);
        game.setParam(CAN_ADD_TEAMS, true);
        game.setParam(REQUIRE_SET_SPAWN, true);
        game.setParam(USE_WORLD_BORDER, true);
        game.setParam(DEFAULT_LIVES, 1);
        game.setParam(ROUNDS_TO_WIN, 3);
        game.addKits("standard", "builder", "archer");
        game.addShops("survival");
        return game;
    }

    private int currentRound = 0;

    public BuyAttackData(String instanceId, String gameTypeId) {
        super(instanceId, gameTypeId);
    }

    @Override
    public void onGameStart(MinecraftServer server) {
        super.onGameStart(server);
    }

    @Override
    public CompoundTag save() {
        CompoundTag nbt = super.save();
        nbt.putInt("currentRound", currentRound);
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        currentRound = nbt.getInt("currentRound");
    }

    @Override
    public void reset(MinecraftServer server) {
        super.reset(server);
        currentRound = 0;
    }

    public int getAttackTime() {
        return getIntParam(ATTACK_TIME);
    }

    public int getBuyTime() {
        return getIntParam(BUY_TIME);
    }

    public int getRoundsToWin() {
        return getIntParam(ROUNDS_TO_WIN);
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public boolean isFirstRound() {
        return getCurrentRound() == 0;
    }

    public void nextRound() {
        ++currentRound;
    }

    public int getAttackEndTime() {
        return getIntParam(ATTACK_END_TIME);
    }

    public boolean isAllowRespawnInBuyPhase() {
        return getBooleanParam(ALLOW_BUY_PHASE_RESPAWN);
    }

    @Override
    public Component getStartGameMessage(MinecraftServer server) {
        return UtilMCText.literal("Buy Attack Phase Game has Started!").setStyle(GOLD_BOLD);
    }

    public boolean hasWinningAgents() {
        return !getAgentsWithScore(getRoundsToWin()).isEmpty();
    }

    public int getBuyRadius() {
        return getIntParam(BUY_RADIUS);
    }

    public void announceWinnersByScore(MinecraftServer server) {
        getAgentsWithScore(getRoundsToWin()).forEach(agent -> agent.onWin(server));
    }

    public boolean allowPvpInBuyPhase() {
        return getBooleanParam(ALLOW_PVP_BUY_PHASE);
    }

    @Override
    protected void registerParams() {
        super.registerParams();
        registerParam(ALLOW_BUY_PHASE_RESPAWN);
        registerParam(ALLOW_PVP_BUY_PHASE);
        registerParam(BUY_TIME);
        registerParam(ATTACK_TIME);
        registerParam(ATTACK_END_TIME);
        registerParam(ROUNDS_TO_WIN);
        registerParam(BUY_RADIUS);
    }
}
