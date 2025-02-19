package com.onewhohears.minigames.minigame.phase.buyattackrounds;

import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.condition.BuyAttackGameWinCondition;
import com.onewhohears.minigames.minigame.condition.PhaseExitCondition;
import com.onewhohears.minigames.minigame.condition.TimeoutPhaseExitCondition;
import com.onewhohears.minigames.minigame.data.BuyAttackData;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;

public class BuyAttackBuyPhase<T extends BuyAttackData> extends GamePhase<T> {

    public BuyAttackBuyPhase(T gameData) {
        this("buy_attack_buy", gameData, new TimeoutPhaseExitCondition<>("buy_attack_buy_end",
                "buy_attack_attack", phase -> phase.getGameData().getBuyTime()),
                new BuyAttackGameWinCondition<>());
    }

    @SafeVarargs
    public BuyAttackBuyPhase(String id, T gameData, PhaseExitCondition<T>...exitConditions) {
        super(id, gameData, exitConditions);
        setCountDown(BuyAttackData::getBuyTime);
    }

    @Override
    public void tickPhase(MinecraftServer server) {
        super.tickPhase(server);
    }

    @Override
    public void tickPlayerAgent(MinecraftServer server, PlayerAgent agent) {
        super.tickPlayerAgent(server, agent);
        buyRadiusCheck(server, agent);
    }

    public void buyRadiusCheck(MinecraftServer server, PlayerAgent agent) {
        int buyRad = getGameData().getBuyRadius();
        if (buyRad > -1 && agent.getRespawnPoint() != null) {
            ServerPlayer player = agent.getPlayer(server);
            if (player == null) return;
            if (player.distanceToSqr(agent.getRespawnPoint()) > buyRad * buyRad) {
                agent.tpToSpawnPoint(server);
            }
        }
    }

    @Override
    public void onReset(MinecraftServer server) {
        super.onReset(server);
    }

    @Override
    public void onStart(MinecraftServer server) {
        super.onStart(server);
        getGameData().setupAllAgents();
        if (getGameData().isFirstRound()) getGameData().onGameStart(server);
        else {
            getGameData().applyAllAgentRespawnPoints(server);
            getGameData().tpPlayersToSpawnPosition(server);
            getGameData().refillAllAgentKits(server);
            getGameData().resetAllPlayerHealth(server);
        }
        getGameData().forAllPOIs(server, (serv, poi) -> poi.onRoundStart(serv));
        getGameData().giveMoneyToAgents(server);
        Component message = UtilMCText.literal("Buy Phase Start!").setStyle(MiniGameData.GOLD_BOLD);
        getGameData().chatToAllPlayers(server, message, SoundEvents.EXPERIENCE_ORB_PICKUP);
    }

    @Override
    public void onStop(MinecraftServer server) {
        super.onStop(server);
    }

    @Override
    public boolean isForceSurvivalMode() {
        return true;
    }

    @Override
    public boolean hasWorldBorder() {
        return getGameData().useWorldBorderDuringGame();
    }

    @Override
    public double getWorldBorderSize() {
        return getGameData().getGameBorderSize();
    }

    @Override
    public boolean allowPVP() {
        return getGameData().allowPvpInBuyPhase();
    }

    @Override
    public boolean looseLiveOnDeath(GameAgent gameAgent, MinecraftServer server) {
        return !getGameData().isAllowRespawnInBuyPhase();
    }

    @Override
    public boolean isBuyPhase() {
        return true;
    }
}
