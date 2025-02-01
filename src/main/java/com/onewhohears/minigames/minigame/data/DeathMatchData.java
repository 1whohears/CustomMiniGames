package com.onewhohears.minigames.minigame.data;

import com.onewhohears.minigames.minigame.phase.deathmatch.DeathMatchEndPhase;
import com.onewhohears.minigames.minigame.phase.deathmatch.DeathMatchPlayPhase;
import com.onewhohears.minigames.minigame.phase.deathmatch.DeathMatchSetupPhase;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;

public class DeathMatchData extends MiniGameData {

	public static final Style GOLD_BOLD = Style.EMPTY.withBold(true).withUnderlined(true).withColor(ChatFormatting.GOLD);

	public static DeathMatchData createSimpleTeamDeathMatch(String instanceId, String gameTypeId, int initialLives) {
		DeathMatchData game = new DeathMatchData(instanceId, gameTypeId);
		game.setPhases(new DeathMatchSetupPhase<>(game), 
				new DeathMatchPlayPhase<>(game), 
				new DeathMatchEndPhase<>(game));
		game.canAddIndividualPlayers = false;
		game.canAddTeams = true;
		game.requiresSetRespawnPos = true;
		game.worldBorderDuringGame = false;
		game.initialLives = initialLives;
		game.addKits("standard", "builder", "archer");
		game.addShops("survival");
		return game;
	}
	
	public static DeathMatchData createSimpleFFADeathMatch(String instanceId, String gameTypeId, int initialLives) {
		DeathMatchData game = new DeathMatchData(instanceId, gameTypeId);
		game.setPhases(new DeathMatchSetupPhase<>(game), 
				new DeathMatchPlayPhase<>(game), 
				new DeathMatchEndPhase<>(game));
		game.canAddIndividualPlayers = true;
		game.canAddTeams = false;
		game.requiresSetRespawnPos = true;
		game.worldBorderDuringGame = false;
		game.initialLives = initialLives;
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
