package com.onewhohears.minigames.minigame.phase.areacontrol;

import com.onewhohears.minigames.minigame.condition.*;
import com.onewhohears.minigames.minigame.data.AreaControlData;
import com.onewhohears.minigames.minigame.phase.buyattackrounds.BuyAttackAttackPhase;

public class AreaControlAttackPhase<T extends AreaControlData> extends BuyAttackAttackPhase<T> {

    public AreaControlAttackPhase(T gameData) {
        this("buy_attack_attack", gameData,
                new AreaControlTimeoutCondition<>(phase -> phase.getGameData().getAttackTime()),
                new BuyAttackGameWinCondition<>(), new AreaControlRoundWinCondition<>(),
                new BuyAttackRoundWinCondition<>());
    }

    @SafeVarargs
    public AreaControlAttackPhase(String id, T gameData, PhaseExitCondition<T>...exitConditions) {
        super(id, gameData, exitConditions);
    }
}
