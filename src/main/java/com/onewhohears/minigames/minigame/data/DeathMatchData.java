package com.onewhohears.minigames.minigame.data;

import com.onewhohears.minigames.minigame.phase.deathmatch.DeathMatchEndPhase;
import com.onewhohears.minigames.minigame.phase.deathmatch.DeathMatchPlayPhase;
import com.onewhohears.minigames.minigame.phase.deathmatch.DeathMatchSetupPhase;

public class DeathMatchData extends MiniGameData {
	
	public static DeathMatchData createSimpleTeamDeathMatch(String id, int initialLives) {
		DeathMatchData game = new DeathMatchData(id);
		game.setPhases(new DeathMatchSetupPhase<>(game), 
				new DeathMatchPlayPhase<>(game), 
				new DeathMatchEndPhase<>(game));
		game.canAddIndividualPlayers = false;
		game.canAddTeams = true;
		game.requiresSetRespawnPos = true;
		game.initialLives = initialLives;
		return game;
	}
	
	public static DeathMatchData createSimpleFFADeathMatch(String id, int initialLives) {
		DeathMatchData game = new DeathMatchData(id);
		game.setPhases(new DeathMatchSetupPhase<>(game), 
				new DeathMatchPlayPhase<>(game), 
				new DeathMatchEndPhase<>(game));
		game.canAddIndividualPlayers = true;
		game.canAddTeams = false;
		game.requiresSetRespawnPos = true;
		game.initialLives = initialLives;
		return game;
	}
	
	protected DeathMatchData(String id) {
		super(id);
	}

}
