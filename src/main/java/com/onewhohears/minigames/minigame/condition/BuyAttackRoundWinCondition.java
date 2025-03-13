package com.onewhohears.minigames.minigame.condition;

import com.onewhohears.minigames.minigame.data.BuyAttackData;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import net.minecraft.server.MinecraftServer;

public class BuyAttackRoundWinCondition<D extends BuyAttackData> extends PhaseExitCondition<D> {

    public BuyAttackRoundWinCondition() {
        super("buy_attack_round_win", "buy_attack_end_attack");
    }

    @Override
    public boolean shouldExit(MinecraftServer server, GamePhase<D> currentPhase) {
        return currentPhase.getGameData().getAgentsWithLives().size() <= 1;
    }

    @Override
    public void onLeave(MinecraftServer server, GamePhase<D> currentPhase) {
        currentPhase.getGameData().awardLivingTeams();
        currentPhase.getGameData().announceScores(server);
    }
}
