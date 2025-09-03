package com.onewhohears.minigames.common.network.toserver;

import com.onewhohears.minigames.common.container.ShopMenu;
import com.onewhohears.minigames.data.shops.GameShop;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class ToServerShopSelect extends BaseC2SMessage {
    private final String shop;
    public ToServerShopSelect(String shop) {
        this.shop = shop;
    }
    public ToServerShopSelect(FriendlyByteBuf buffer){
        shop = buffer.readUtf();
    }
    @Override
    public MessageType getType() {
        return null;
    }
    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(shop);
    }
    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) return;
            List<PlayerAgent> agents = MiniGameManager.get().getActiveGamePlayerAgents(player);
            for (PlayerAgent agent : agents) {
                if (!agent.canOpenShop(player.getServer(), shop)) {
                    Component message = Component.literal("You cannot open this shop!");
                    player.displayClientMessage(message, true);
                    return;
                }
                GameShop gs = MiniGameShopsManager.get().get(shop);
                if (gs == null) {
                    Component message = Component.literal("This shop does not exist!");
                    player.displayClientMessage(message, true);
                    return;
                }
                ShopMenu.openScreen(player, gs);
            }
        });
    }
}
