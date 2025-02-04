package com.onewhohears.minigames.minigame.phase.flag;

import com.onewhohears.minigames.minigame.condition.*;
import com.onewhohears.minigames.minigame.data.KillFlagData;
import com.onewhohears.minigames.minigame.phase.buyattackrounds.BuyAttackAttackPhase;

public class KillFlagAttackPhase<T extends KillFlagData> extends BuyAttackAttackPhase<T> {

    public KillFlagAttackPhase(T gameData) {
        this("buy_attack_attack", gameData,
                new KillFlagAttackTimeoutCondition<>(phase -> phase.getGameData().getAttackTime()),
                new BuyAttackGameWinCondition<>(), new KillFlagAttackersDeadCondition<>(),
                new KillFlagFlagsDeadCondition<>());
    }

    @SafeVarargs
    public KillFlagAttackPhase(String id, T gameData, PhaseExitCondition<T>... exitConditions) {
        super(id, gameData, exitConditions);
    }
}
