package com.onewhohears.minigames.minigame.condition;

import com.onewhohears.minigames.minigame.data.KillFlagData;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import net.minecraft.server.MinecraftServer;

public class KillFlagAttackersDeadCondition<D extends KillFlagData> extends BuyAttackRoundWinCondition<D> {

    public KillFlagAttackersDeadCondition() {

    }

    @Override
    public boolean shouldExit(MinecraftServer server, GamePhase<D> currentPhase) {
        return currentPhase.getGameData().getLivingAttackers().isEmpty();
    }

    @Override
    public void onLeave(MinecraftServer server, GamePhase<D> currentPhase) {
        currentPhase.getGameData().awardLivingFlagTeams();
        currentPhase.getGameData().announceScores(server);
    }

}
