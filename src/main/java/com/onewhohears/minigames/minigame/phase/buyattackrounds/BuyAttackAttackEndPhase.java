package com.onewhohears.minigames.minigame.phase.buyattackrounds;

import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.condition.BuyAttackGameWinCondition;
import com.onewhohears.minigames.minigame.condition.PhaseExitCondition;
import com.onewhohears.minigames.minigame.condition.TimeoutPhaseExitCondition;
import com.onewhohears.minigames.minigame.data.BuyAttackData;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;

public class BuyAttackAttackEndPhase<T extends BuyAttackData> extends GamePhase<T> {

    public BuyAttackAttackEndPhase(T gameData) {
        this(gameData, new TimeoutPhaseExitCondition<>("buy_attack_attack_end",
                "buy_attack_buy", gameData.getAttackEndTime()),
                new BuyAttackGameWinCondition<>());
    }

    @SafeVarargs
    public BuyAttackAttackEndPhase(T gameData, PhaseExitCondition<T>...exitConditions) {
        super("buy_attack_end_attack", gameData, exitConditions);
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
        Component message = UtilMCText.literal("Buy Phase will Start Soon!").setStyle(MiniGameData.GOLD_BOLD);
        getGameData().chatToAllPlayers(server, message, SoundEvents.EXPERIENCE_ORB_PICKUP);
    }

    @Override
    public void onStop(MinecraftServer server) {
        super.onStop(server);
        getGameData().nextRound();
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
    public boolean canAgentOpenShop(GameAgent agent, String shop) {
        return false;
    }
}
