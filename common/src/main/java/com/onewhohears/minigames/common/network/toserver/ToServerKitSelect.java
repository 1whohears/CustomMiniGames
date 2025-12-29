package com.onewhohears.minigames.common.network.toserver;

import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static com.onewhohears.minigames.common.network.CMGPacketHandler.C2S_KIT;

public class ToServerKitSelect extends BaseC2SMessage {
    private final String kit;
    public ToServerKitSelect(String kit) {
        this.kit = kit;
    }
    public ToServerKitSelect(FriendlyByteBuf buffer){
        kit = buffer.readUtf();
    }
    @Override
    public MessageType getType() {
        return C2S_KIT;
    }
    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(kit);
    }
    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) return;
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
    }
}
