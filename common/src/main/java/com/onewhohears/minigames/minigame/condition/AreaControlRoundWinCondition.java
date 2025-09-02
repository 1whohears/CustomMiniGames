package com.onewhohears.minigames.minigame.condition;

import com.onewhohears.minigames.minigame.data.AreaControlData;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import net.minecraft.server.MinecraftServer;

public class AreaControlRoundWinCondition<D extends AreaControlData> extends PhaseExitCondition<D> {

    public AreaControlRoundWinCondition() {
        super("area_control_round_win", "buy_attack_end_attack");
    }

    @Override
    public boolean shouldExit(MinecraftServer server, GamePhase<D> currentPhase) {
        return currentPhase.getGameData().allAreasControlledBySame(server);
    }

    @Override
    public void onLeave(MinecraftServer server, GamePhase<D> currentPhase) {
        currentPhase.getGameData().awardByNumControlledAreas(server);
        currentPhase.getGameData().announceScores(server);
    }
}
