package com.onewhohears.minigames.minigame.phase.buyattackrounds;

import com.onewhohears.minigames.minigame.data.BuyAttackData;
import com.onewhohears.minigames.minigame.phase.SetupPhase;

public class BuyAttackSetupPhase<T extends BuyAttackData> extends SetupPhase<T> {

	public BuyAttackSetupPhase(T gameData) {
		this("buy_attack_setup", gameData);
	}

	public BuyAttackSetupPhase(String id, T gameData) {
		super(id, gameData);
	}

}
