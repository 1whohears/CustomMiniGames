package com.onewhohears.minigames.minigame.data;

import com.onewhohears.minigames.minigame.phase.buyattackrounds.*;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class BuyAttackData extends MiniGameData {

    public static BuyAttackData createBuyAttackPhaseMatch(String instanceId, String gameTypeId) {
        BuyAttackData game = new BuyAttackData(instanceId, gameTypeId);
        game.setPhases(new BuyAttackSetupPhase<>(game),
                new BuyAttackBuyPhase<>(game),
                new BuyAttackAttackPhase<>(game),
                new BuyAttackAttackEndPhase<>(game),
                new BuyAttackEndPhase<>(game));
        game.canAddIndividualPlayers = true;
        game.canAddTeams = true;
        game.requiresSetRespawnPos = true;
        game.worldBorderDuringGame = true;
        game.defaultInitialLives = 1;
        game.roundsToWin = 3;
        game.addKits("standard", "builder", "archer");
        game.addShops("survival");
        return game;
    }

    protected int buyTime = 400;
    protected int attackTime = 4800;
    protected int attackEndTime = 200;
    protected int roundsToWin = 3;
    protected int buyRadius = 24;
    protected boolean allowRespawnInBuyPhase = true;
    protected boolean allowPvpInBuyPhase = false;

    private int currentRound = 0;

    public BuyAttackData(String instanceId, String gameTypeId) {
        super(instanceId, gameTypeId);
        this.moneyPerRound = 20;
    }

    @Override
    public void onGameStart(MinecraftServer server) {
        super.onGameStart(server);
    }

    @Override
    public CompoundTag save() {
        CompoundTag nbt = super.save();
        nbt.putInt("buyTime", buyTime);
        nbt.putInt("attackTime", attackTime);
        nbt.putInt("roundsToWin", roundsToWin);
        nbt.putInt("currentRound", currentRound);
        nbt.putInt("attackEndTime", attackEndTime);
        nbt.putInt("buyRadius", buyRadius);
        nbt.putBoolean("allowRespawnInBuyPhase", allowRespawnInBuyPhase);
        nbt.putBoolean("allowPvpInBuyPhase", allowPvpInBuyPhase);
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        buyTime = nbt.getInt("buyTime");
        attackTime = nbt.getInt("attackTime");
        roundsToWin = nbt.getInt("roundsToWin");
        currentRound = nbt.getInt("currentRound");
        attackEndTime = nbt.getInt("attackEndTime");
        buyRadius = nbt.getInt("buyRadius");
        allowRespawnInBuyPhase = nbt.getBoolean("allowRespawnInBuyPhase");
        allowPvpInBuyPhase = nbt.getBoolean("allowPvpInBuyPhase");
    }

    @Override
    public void reset(MinecraftServer server) {
        super.reset(server);
        currentRound = 0;
    }

    public int getAttackTime() {
        return attackTime;
    }

    public int getBuyTime() {
        return buyTime;
    }

    public int getRoundsToWin() {
        return roundsToWin;
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
        return attackEndTime;
    }

    public void setAttackEndTime(int attackEndTime) {
        this.attackEndTime = attackEndTime;
    }

    public void setBuyTime(int buyTime) {
        this.buyTime = buyTime;
    }

    public void setAttackTime(int attackTime) {
        this.attackTime = attackTime;
    }

    public void setRoundsToWin(int roundsToWin) {
        this.roundsToWin = roundsToWin;
    }

    public void setAllowRespawnInBuyPhase(boolean allow) {
        this.allowRespawnInBuyPhase = allow;
    }

    public boolean isAllowRespawnInBuyPhase() {
        return allowRespawnInBuyPhase;
    }

    @Override
    public Component getStartGameMessage(MinecraftServer server) {
        return UtilMCText.literal("Buy Attack Phase Game has Started!").setStyle(GOLD_BOLD);
    }

    public boolean hasWinningAgents() {
        return !getAgentsWithScore(getRoundsToWin()).isEmpty();
    }

    public int getBuyRadius() {
        return buyRadius;
    }

    public void setBuyRadius(int buyRadius) {
        this.buyRadius = buyRadius;
    }

    public void announceWinnersByScore(MinecraftServer server) {
        getAgentsWithScore(getRoundsToWin()).forEach(agent -> agent.onWin(server));
    }

    public boolean allowPvpInBuyPhase() {
        return allowPvpInBuyPhase;
    }

    public void setAllowPvpInBuyPhase(boolean allowPvpInBuyPhase) {
        this.allowPvpInBuyPhase = allowPvpInBuyPhase;
    }

}
