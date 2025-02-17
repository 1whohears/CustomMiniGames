package com.onewhohears.minigames.minigame.phase.attackdefend;

import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.condition.BuyAttackGameWinCondition;
import com.onewhohears.minigames.minigame.condition.PhaseExitCondition;
import com.onewhohears.minigames.minigame.condition.TimeoutPhaseExitCondition;
import com.onewhohears.minigames.minigame.data.AttackDefendData;
import com.onewhohears.minigames.minigame.phase.buyattackrounds.BuyAttackBuyPhase;

public class AttackDefendBuyPhase<T extends AttackDefendData> extends BuyAttackBuyPhase<T> {

    public AttackDefendBuyPhase(T gameData) {
        this("buy_attack_buy", gameData, new TimeoutPhaseExitCondition<>("buy_attack_buy_end",
                        "buy_attack_attack", phase -> phase.getGameData().getBuyTime()),
                new BuyAttackGameWinCondition<>());
    }

    @SafeVarargs
    public AttackDefendBuyPhase(String id, T gameData, PhaseExitCondition<T>... exitConditions) {
        super(id, gameData, exitConditions);
    }

    @Override
    public boolean canAgentOpenShop(GameAgent agent, String shop) {
        return getGameData().canOpenAttackDefendShop(agent, shop);
    }
}
