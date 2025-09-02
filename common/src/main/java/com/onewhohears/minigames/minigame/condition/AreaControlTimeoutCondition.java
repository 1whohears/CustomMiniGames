package com.onewhohears.minigames.minigame.condition;

import com.onewhohears.minigames.minigame.data.AreaControlData;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import net.minecraft.server.MinecraftServer;

import java.util.function.Function;

public class AreaControlTimeoutCondition<D extends AreaControlData> extends TimeoutPhaseExitCondition<D> {

    public AreaControlTimeoutCondition(Function<GamePhase<D>, Integer> time, String timeEndTranslatable) {
        super("buy_attack_attack_end", "buy_attack_end_attack", time, timeEndTranslatable);
    }

    public AreaControlTimeoutCondition(Function<GamePhase<D>, Integer> time) {
        this(time, null);
    }

    @Override
    public void onLeave(MinecraftServer server, GamePhase<D> currentPhase) {
        currentPhase.getGameData().awardByNumControlledAreas(server);
        currentPhase.getGameData().announceScores(server);
    }
}
