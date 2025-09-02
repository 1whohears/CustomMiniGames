package com.onewhohears.minigames.minigame.phase.attackdefend;

import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.condition.*;
import com.onewhohears.minigames.minigame.data.AttackDefendData;
import com.onewhohears.minigames.minigame.phase.buyattackrounds.BuyAttackAttackPhase;
import net.minecraft.server.MinecraftServer;

public class AttackDefendAttackPhase<T extends AttackDefendData> extends BuyAttackAttackPhase<T> {

    public AttackDefendAttackPhase(T gameData) {
        this("buy_attack_attack", gameData,
                new DefendTimeoutCondition<>(phase -> phase.getGameData().getAttackTime()),
                new BuyAttackGameWinCondition<>(), new BuyAttackRoundWinCondition<>());
    }

    @SafeVarargs
    public AttackDefendAttackPhase(String id, T gameData, PhaseExitCondition<T>...exitConditions) {
        super(id, gameData, exitConditions);
    }

    @Override
    public boolean canAgentOpenShop(MinecraftServer server, GameAgent agent, String shop) {
        return getGameData().canOpenAttackDefendShop(server, agent, shop);
    }

}
