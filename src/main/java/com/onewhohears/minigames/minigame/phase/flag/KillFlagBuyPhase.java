package com.onewhohears.minigames.minigame.phase.flag;

import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.condition.*;
import com.onewhohears.minigames.minigame.data.KillFlagData;
import com.onewhohears.minigames.minigame.phase.buyattackrounds.BuyAttackBuyPhase;
import net.minecraft.server.MinecraftServer;

public class KillFlagBuyPhase<T extends KillFlagData> extends BuyAttackBuyPhase<T> {

    public KillFlagBuyPhase(T gameData) {
        this("buy_attack_buy", gameData, new TimeoutPhaseExitCondition<>("buy_attack_buy_end",
                        "buy_attack_attack", gameData.getBuyTime()),
                new BuyAttackGameWinCondition<>());
    }

    @SafeVarargs
    public KillFlagBuyPhase(String id, T gameData, PhaseExitCondition<T>... exitConditions) {
        super(id, gameData, exitConditions);
    }

    @Override
    public void onStart(MinecraftServer server) {
        super.onStart(server);
        getGameData().resetFlags(server);
    }

    @Override
    public boolean canAgentOpenShop(GameAgent agent, String shop) {
        if (getGameData().isAttacker(agent.getId()) && getGameData().isAttackerShop(shop)) return true;
        else if (getGameData().isDefender(agent.getId()) && getGameData().isDefenderShop(shop)) return true;
        return getGameData().hasShop(shop);
    }
}
