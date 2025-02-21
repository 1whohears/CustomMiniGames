package com.onewhohears.minigames.minigame.data;

import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.phase.attackdefend.AttackDefendAttackPhase;
import com.onewhohears.minigames.minigame.phase.attackdefend.AttackDefendBuyPhase;
import com.onewhohears.minigames.minigame.phase.buyattackrounds.*;
import net.minecraft.nbt.CompoundTag;

public class LastStandData extends AttackDefendData {

    public static BuyAttackData createLastStandMatch(String instanceId, String gameTypeId) {
        LastStandData game = new LastStandData(instanceId, gameTypeId);
        game.setPhases(new BuyAttackSetupPhase<>(game),
                new AttackDefendBuyPhase<>(game),
                new AttackDefendAttackPhase<>(game),
                new BuyAttackAttackEndPhase<>(game),
                new BuyAttackEndPhase<>(game));
        game.canAddIndividualPlayers = true;
        game.canAddTeams = true;
        game.requiresSetRespawnPos = true;
        game.worldBorderDuringGame = true;
        game.defaultInitialLives = 1;
        game.roundsToWin = 3;
        game.buyTime = 1200;
        game.attackTime = 7200;
        game.attackersShareLives = true;
        return game;
    }

    public int initialAttackerLives = 50;

    public LastStandData(String instanceId, String gameTypeId) {
        super(instanceId, gameTypeId);
    }

    @Override
    public CompoundTag save() {
        CompoundTag nbt = super.save();
        nbt.putInt("initialAttackerLives", initialAttackerLives);
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        initialAttackerLives = nbt.getInt("initialAttackerLives");
    }

    @Override
    public boolean addAttacker(String id) {
        if (!super.addAttacker(id)) return false;
        GameAgent agent = getAgentById(id);
        if (agent == null) return false;
        agent.setInitialLives(initialAttackerLives);
        return true;
    }
}
