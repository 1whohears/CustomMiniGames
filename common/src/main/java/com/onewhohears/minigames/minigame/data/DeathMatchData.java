package com.onewhohears.minigames.minigame.data;

import com.onewhohears.minigames.minigame.phase.deathmatch.DeathMatchEndPhase;
import com.onewhohears.minigames.minigame.phase.deathmatch.DeathMatchPlayPhase;
import com.onewhohears.minigames.minigame.phase.deathmatch.DeathMatchSetupPhase;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import static com.onewhohears.minigames.minigame.param.MiniGameParamTypes.*;

public class DeathMatchData extends MiniGameData {

	public static DeathMatchData createSimpleTeamDeathMatch(String instanceId, String gameTypeId, int initialLives) {
		DeathMatchData game = new DeathMatchData(instanceId, gameTypeId);
		game.setPhases(new DeathMatchSetupPhase<>(game), 
				new DeathMatchPlayPhase<>(game), 
				new DeathMatchEndPhase<>(game));
		game.setParam(CAN_ADD_PLAYERS, false);
		game.setParam(CAN_ADD_TEAMS, true);
		game.setParam(REQUIRE_SET_SPAWN, true);
		game.setParam(USE_WORLD_BORDER, false);
		game.setParam(DEFAULT_LIVES, initialLives);
		game.addKits("standard", "builder", "archer");
		game.addShops("survival");
		return game;
	}
	
	public static DeathMatchData createSimpleFFADeathMatch(String instanceId, String gameTypeId, int initialLives) {
		DeathMatchData game = new DeathMatchData(instanceId, gameTypeId);
		game.setPhases(new DeathMatchSetupPhase<>(game), 
				new DeathMatchPlayPhase<>(game), 
				new DeathMatchEndPhase<>(game));
		game.setParam(CAN_ADD_PLAYERS, true);
		game.setParam(CAN_ADD_TEAMS, false);
		game.setParam(REQUIRE_SET_SPAWN, true);
		game.setParam(USE_WORLD_BORDER, false);
		game.setParam(DEFAULT_LIVES, initialLives);
		game.addKits("standard", "builder", "archer");
		game.addShops("survival");
		return game;
	}
	
	protected DeathMatchData(String instanceId, String gameTypeId) {
		super(instanceId, gameTypeId);
	}

	@Override
	public Component getStartGameMessage(MinecraftServer server) {
		return UtilMCText.literal("Death Match has Started!").setStyle(GOLD_BOLD);
	}

}
