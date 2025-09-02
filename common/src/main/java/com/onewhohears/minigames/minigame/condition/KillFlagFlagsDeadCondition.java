package com.onewhohears.minigames.minigame.condition;

import com.onewhohears.minigames.minigame.data.KillFlagData;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import net.minecraft.server.MinecraftServer;

public class KillFlagFlagsDeadCondition<D extends KillFlagData> extends BuyAttackRoundWinCondition<D> {

    public KillFlagFlagsDeadCondition() {

    }

    @Override
    public boolean shouldExit(MinecraftServer server, GamePhase<D> currentPhase) {
        return currentPhase.getGameData().getLivingFlags().isEmpty();
    }

    @Override
    public void onLeave(MinecraftServer server, GamePhase<D> currentPhase) {
        currentPhase.getGameData().awardAllAttackers();
        currentPhase.getGameData().announceScores(server);
    }

}
