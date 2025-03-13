package com.onewhohears.minigames.minigame.phase.buyattackrounds;

import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.condition.AttackPhaseTimeoutCondition;
import com.onewhohears.minigames.minigame.condition.BuyAttackGameWinCondition;
import com.onewhohears.minigames.minigame.condition.BuyAttackRoundWinCondition;
import com.onewhohears.minigames.minigame.condition.PhaseExitCondition;
import com.onewhohears.minigames.minigame.data.BuyAttackData;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;

public class BuyAttackAttackPhase<T extends BuyAttackData> extends GamePhase<T> {

    public BuyAttackAttackPhase(T gameData) {
        this("buy_attack_attack", gameData,
                new AttackPhaseTimeoutCondition<>(phase -> phase.getGameData().getAttackTime()),
                new BuyAttackGameWinCondition<>(), new BuyAttackRoundWinCondition<>());
    }

    @SafeVarargs
    public BuyAttackAttackPhase(String id, T gameData, PhaseExitCondition<T>...exitConditions) {
        super(id, gameData, exitConditions);
        setCountDown(BuyAttackData::getAttackTime);
    }

    @Override
    public void tickPhase(MinecraftServer server) {
        super.tickPhase(server);
    }

    @Override
    public void onReset(MinecraftServer server) {
        super.onReset(server);
    }

    @Override
    public void onStart(MinecraftServer server) {
        super.onStart(server);
        getGameData().resetAllPlayerHealth(server);
        Component message = UtilMCText.literal("Attack Phase Start!").setStyle(MiniGameData.GOLD_BOLD);
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
    public boolean canAgentOpenShop(MinecraftServer server, GameAgent agent, String shop) {
        return getGameData().canOpenBuyAttackPhaseShop(server, agent, shop);
    }

    @Override
    public boolean isAttackPhase() {
        return true;
    }
}
