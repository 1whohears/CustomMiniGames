package com.onewhohears.minigames.minigame.condition;

import com.onewhohears.minigames.minigame.data.BuyAttackData;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import net.minecraft.server.MinecraftServer;

import java.util.function.Function;

public class AttackPhaseTimeoutCondition<D extends BuyAttackData> extends TimeoutPhaseExitCondition<D> {

    public AttackPhaseTimeoutCondition(Function<GamePhase<D>, Integer> time, String timeEndTranslatable) {
        super("buy_attack_attack_end", "buy_attack_end_attack", time, timeEndTranslatable);
    }

    public AttackPhaseTimeoutCondition(Function<GamePhase<D>, Integer> time) {
        this(time, null);
    }

    @Override
    public void onLeave(MinecraftServer server, GamePhase<D> currentPhase) {
        currentPhase.getGameData().awardLivingTeamsTie();
        currentPhase.getGameData().announceScores(server);
    }
}
