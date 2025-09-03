package com.onewhohears.minigames.common.network.toserver;

import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.TeamAgent;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.onewholibs.util.UtilMCText;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class ToServerGameSelect extends BaseC2SMessage {
    private final String game, team;
    public ToServerGameSelect(@NotNull String game, @NotNull String team) {
        this.game = game;
        this.team = team;
    }
    public ToServerGameSelect(FriendlyByteBuf buffer){
        game = buffer.readUtf();
        team = buffer.readUtf();
    }
    @Override
    public MessageType getType() {
        return null;
    }
    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(game);
        buffer.writeUtf(team);
    }
    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) return;
            MiniGameData data = MiniGameManager.get().getRunningGame(game);
            if (data == null) return;
            if (team.isEmpty() && data.canAddIndividualPlayers()) {
                data.getAddIndividualPlayer(player);
                Component message = UtilMCText.literal("Added solo player ")
                        .append(player.getDisplayName())
                        .append(" to the game "+game);
                data.chatToAllPlayers(player.getServer(), message);
                return;
            } else if (!team.isEmpty() && data.canAddTeams() && data.hasAgentById(team)) {
                TeamAgent agent = data.getTeamAgentByName(team);
                if (agent != null && agent.addPlayer(player.getServer(), player)) {
                    Component message = UtilMCText.literal("Added player ")
                            .append(player.getDisplayName())
                            .append(" to the team "+team+" for game "+game);
                    data.chatToAllPlayers(player.getServer(), message);
                    return;
                }
            }
            Component message = UtilMCText.literal("You could not be added to the game "+game);
            player.sendSystemMessage(message);
        });
    }
}
