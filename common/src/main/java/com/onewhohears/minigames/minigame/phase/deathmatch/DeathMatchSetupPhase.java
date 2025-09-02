package com.onewhohears.minigames.minigame.phase.deathmatch;

import com.onewhohears.minigames.minigame.data.DeathMatchData;
import com.onewhohears.minigames.minigame.phase.SetupPhase;

public class DeathMatchSetupPhase<T extends DeathMatchData> extends SetupPhase<T> {

	public DeathMatchSetupPhase(T gameData) {
		this("death_match_setup", gameData);
	}
	
	public DeathMatchSetupPhase(String id, T gameData) {
		super(id, gameData);
	}

}
