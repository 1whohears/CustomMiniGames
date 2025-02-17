package com.onewhohears.minigames.minigame.condition;

import com.onewhohears.minigames.minigame.data.AttackDefendData;
import com.onewhohears.minigames.minigame.data.BuyAttackData;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import net.minecraft.server.MinecraftServer;

import java.util.function.Function;

public class DefendTimeoutCondition<D extends AttackDefendData> extends TimeoutPhaseExitCondition<D> {

    public DefendTimeoutCondition(Function<GamePhase<D>, Integer> time, String timeEndTranslatable) {
        super("buy_attack_attack_end", "buy_attack_end_attack", time, timeEndTranslatable);
    }

    public DefendTimeoutCondition(Function<GamePhase<D>, Integer> time) {
        this(time, null);
    }

    @Override
    public void onLeave(MinecraftServer server, GamePhase<D> currentPhase) {
        currentPhase.getGameData().awardAllDefenders();
        currentPhase.getGameData().announceScores(server);
    }
}
