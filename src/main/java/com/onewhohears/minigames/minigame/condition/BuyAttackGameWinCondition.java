package com.onewhohears.minigames.minigame.condition;

import com.onewhohears.minigames.minigame.data.BuyAttackData;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import net.minecraft.server.MinecraftServer;

public class BuyAttackGameWinCondition<D extends BuyAttackData> extends PhaseExitCondition<D> {

    public BuyAttackGameWinCondition() {
        super("buy_attack_game_win", "buy_attack_end");
    }

    @Override
    public boolean shouldExit(MinecraftServer server, GamePhase<D> currentPhase) {
        return currentPhase.getGameData().getWinningAgent().isPresent();
    }

    @Override
    public void onLeave(MinecraftServer server, GamePhase<D> currentPhase) {
        currentPhase.getGameData().announceScores(server);
        currentPhase.getGameData().announceWinnersByScore(server);
    }
}
