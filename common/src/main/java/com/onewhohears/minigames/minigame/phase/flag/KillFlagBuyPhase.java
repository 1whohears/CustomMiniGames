package com.onewhohears.minigames.minigame.phase.flag;

import com.onewhohears.minigames.minigame.condition.*;
import com.onewhohears.minigames.minigame.data.KillFlagData;
import com.onewhohears.minigames.minigame.phase.attackdefend.AttackDefendBuyPhase;
import net.minecraft.server.MinecraftServer;

public class KillFlagBuyPhase<T extends KillFlagData> extends AttackDefendBuyPhase<T> {

    public KillFlagBuyPhase(T gameData) {
        this("buy_attack_buy", gameData, new TimeoutPhaseExitCondition<>("buy_attack_buy_end",
                        "buy_attack_attack", phase -> phase.getGameData().getBuyTime()),
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
}
