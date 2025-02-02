package com.onewhohears.minigames.minigame.condition;

import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.data.BuyAttackData;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import net.minecraft.server.MinecraftServer;

public class BuyAttackRoundWinCondition<D extends BuyAttackData> extends PhaseExitCondition<D> {

    public BuyAttackRoundWinCondition() {
        super("buy_attack_round_win", "buy_attack_end_attack");
    }

    @Override
    public boolean shouldExit(MinecraftServer server, GamePhase<D> currentPhase) {
        return currentPhase.getGameData().getLivingAgents().size() <= 1;
    }

    @Override
    public void onExit(MinecraftServer server, GamePhase<D> currentPhase) {
        super.onExit(server, currentPhase);
        for (GameAgent agent : currentPhase.getGameData().getLivingAgents())
            agent.addScore(1);
        currentPhase.getGameData().announceScores(server);
    }
}
