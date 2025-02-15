package com.onewhohears.minigames.minigame.condition;

import com.onewhohears.minigames.minigame.data.BuyAttackData;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import net.minecraft.server.MinecraftServer;

import java.util.function.Function;

public class KillFlagAttackTimeoutCondition<D extends BuyAttackData> extends TimeoutPhaseExitCondition<D> {

    public KillFlagAttackTimeoutCondition(Function<GamePhase<D>, Integer> time, String timeEndTranslatable) {
        super("buy_attack_attack_end", "buy_attack_end_attack", time, timeEndTranslatable);
    }

    public KillFlagAttackTimeoutCondition(Function<GamePhase<D>, Integer> time) {
        this(time, null);
    }

    @Override
    public void onLeave(MinecraftServer server, GamePhase<D> currentPhase) {
        currentPhase.getGameData().awardLivingFlagTeams();
        currentPhase.getGameData().announceScores(server);
    }
}
