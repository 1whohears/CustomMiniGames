package com.onewhohears.minigames.minigame.agent;

import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.scores.PlayerTeam;

import javax.annotation.Nullable;
import java.util.Collection;

public class VanillaTeamAgent extends TeamAgent {

	public VanillaTeamAgent(String type, String teamName, MiniGameData gameData) {
		super(type, teamName, gameData);
	}

	@Nullable
	public PlayerTeam getTeam(MinecraftServer server) {
		Collection<PlayerTeam> teams = server.getScoreboard().getPlayerTeams();
		for (PlayerTeam team : teams)
			if (team.getName().equals(getId()))
				return team;
		return null;
	}

	@Override
	public boolean teamExists(MinecraftServer server) {
		return getTeam(server) != null;
	}

	@Override
	protected void updatePlayerAgentMap(MinecraftServer server) {
		PlayerTeam team = getTeam(server);
		if (team == null) return;
		Collection<String> usernames = team.getPlayers();
		playerAgents.entrySet().removeIf(entry ->
				!usernames.contains(entry.getKey()) && entry.getValue().canTickAgent(server));
		for (String username : usernames) {
			if (playerAgents.containsKey(username)) continue;
			ServerPlayer player = server.getPlayerList().getPlayerByName(username);
			if (player != null) {
				PlayerAgent agent = getGameData().createPlayerAgent(player);
				agent.setTeamAgent(this);
				playerAgents.put(username, agent);
			}
		}
	}

	@Override
	public void onWin(MinecraftServer server) {
		PlayerTeam team = getTeam(server);
		if (team == null) return;
		Style style = team.getDisplayName().getStyle().withBold(true).withUnderlined(true);
		Component message = Component.empty().append(team.getFormattedDisplayName())
				.append(" is the winning team!").setStyle(style);
		getGameData().chatToAllPlayers(server, message, SoundEvents.FIREWORK_ROCKET_LAUNCH);
	}

	@Override
	public Component getDebugInfo(MinecraftServer server) {
		MutableComponent message = Component.literal("[");
		PlayerTeam pt = getTeam(server);
		if (pt == null) message.append(getId());
		else message.append(pt.getDisplayName());
		message.append("]");
		return message;
	}

	@Override
	public Component getDisplayName(MinecraftServer server) {
		PlayerTeam pt = getTeam(server);
		if (pt == null) return UtilMCText.literal(getId());
		return pt.getDisplayName();
	}

	@Override
	public int getColor(MinecraftServer server) {
		PlayerTeam pt = getTeam(server);
		if (pt == null) return 0xffffff;
		if (pt.getColor().getColor() == null) return 0xffffff;
		return pt.getColor().getColor();
	}

	@Override
	public boolean addPlayer(MinecraftServer server, ServerPlayer player) {
		PlayerTeam pt = getTeam(server);
		if (pt == null) return false;
		server.getScoreboard().addPlayerToTeam(player.getScoreboardName(), pt);
		return true;
	}

	@Override @Nullable
	public PlayerTeam getPlayerTeamForDisplay(MinecraftServer server) {
		return getTeam(server);
	}
}
