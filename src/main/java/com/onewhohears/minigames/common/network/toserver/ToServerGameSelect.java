package com.onewhohears.minigames.common.network.toserver;

import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.TeamAgent;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ToServerGameSelect {
    private final String game, team;
    public ToServerGameSelect(@NotNull String game, @NotNull String team) {
        this.game = game;
        this.team = team;
    }
    public ToServerGameSelect(FriendlyByteBuf buffer){
        game = buffer.readUtf();
        team = buffer.readUtf();
    }
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(game);
        buffer.writeUtf(team);
    }
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            success.set(true);
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
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
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
