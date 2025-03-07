package com.onewhohears.minigames.minigame.data;

import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.phase.attackdefend.AttackDefendAttackPhase;
import com.onewhohears.minigames.minigame.phase.attackdefend.AttackDefendBuyPhase;
import com.onewhohears.minigames.minigame.phase.buyattackrounds.*;

import static com.onewhohears.minigames.minigame.param.MiniGameParamTypes.*;

public class LastStandData extends AttackDefendData {

    public static BuyAttackData createLastStandMatch(String instanceId, String gameTypeId) {
        LastStandData game = new LastStandData(instanceId, gameTypeId);
        game.setPhases(new BuyAttackSetupPhase<>(game),
                new AttackDefendBuyPhase<>(game),
                new AttackDefendAttackPhase<>(game),
                new BuyAttackAttackEndPhase<>(game),
                new BuyAttackEndPhase<>(game));
        game.setParam(CAN_ADD_PLAYERS, true);
        game.setParam(CAN_ADD_TEAMS, true);
        game.setParam(REQUIRE_SET_SPAWN, true);
        game.setParam(USE_WORLD_BORDER, false);
        game.setParam(DEFAULT_LIVES, 1);
        game.setParam(ROUNDS_TO_WIN, 3);
        game.setParam(BUY_TIME, 1200);
        game.setParam(ATTACK_TIME, 7200);
        game.setParam(ATTACKERS_SHARE_LIVES, true);
        return game;
    }

    public LastStandData(String instanceId, String gameTypeId) {
        super(instanceId, gameTypeId);
    }

    @Override
    public boolean addAttacker(String id) {
        if (!super.addAttacker(id)) return false;
        GameAgent agent = getAgentById(id);
        if (agent == null) return false;
        agent.setInitialLives(getIntParam(INIT_ATTACKER_LIVES));
        return true;
    }

    @Override
    protected void registerParams() {
        super.registerParams();
        registerParam(INIT_ATTACKER_LIVES);
    }
}
