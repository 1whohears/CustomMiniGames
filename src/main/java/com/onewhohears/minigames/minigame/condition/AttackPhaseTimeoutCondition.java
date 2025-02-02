package com.onewhohears.minigames.minigame.condition;

import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.data.BuyAttackData;
import com.onewhohears.minigames.minigame.phase.GamePhase;
import net.minecraft.server.MinecraftServer;

public class AttackPhaseTimeoutCondition<D extends BuyAttackData> extends TimeoutPhaseExitCondition<D> {

    public AttackPhaseTimeoutCondition(int time, String timeEndTranslatable) {
        super("buy_attack_attack_end", "buy_attack_end_attack", time, timeEndTranslatable);
    }

    public AttackPhaseTimeoutCondition(int time) {
        this(time, null);
    }

    @Override
    public void onExit(MinecraftServer server, GamePhase<D> currentPhase) {
        super.onExit(server, currentPhase);
        double numLiving = currentPhase.getGameData().getLivingAgents().size();
        for (GameAgent agent : currentPhase.getGameData().getLivingAgents())
            agent.addScore(1d/numLiving);
        currentPhase.getGameData().announceScores(server);
    }
}
