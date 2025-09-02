package com.onewhohears.minigames.common.network.toserver;

import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ToServerKitSelect {
    private final String kit;
    public ToServerKitSelect(String kit) {
        this.kit = kit;
    }
    public ToServerKitSelect(FriendlyByteBuf buffer){
        kit = buffer.readUtf();
    }
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(kit);
    }
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            success.set(true);
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            List<PlayerAgent> agents = MiniGameManager.get().getActiveGamePlayerAgents(player);
            for (PlayerAgent agent : agents) {
                if (!agent.canUseKit(kit)) {
                    Component message = Component.literal("You cannot use this kit!");
                    player.displayClientMessage(message, true);
                    return;
                }
                agent.setSelectedKit(kit);
                Component message = Component.literal("Changed kit to "+kit);
                player.displayClientMessage(message, true);
            }
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
