package com.onewhohears.minigames.common.network.toserver;

import com.onewhohears.minigames.common.container.ShopMenu;
import com.onewhohears.minigames.data.shops.GameShop;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ToServerShopSelect {
    private final String shop;
    public ToServerShopSelect(String shop) {
        this.shop = shop;
    }
    public ToServerShopSelect(FriendlyByteBuf buffer){
        shop = buffer.readUtf();
    }
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(shop);
    }
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            success.set(true);
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            List<PlayerAgent> agents = MiniGameManager.get().getActiveGamePlayerAgents(player);
            for (PlayerAgent agent : agents) {
                if (!agent.canOpenShop(shop)) {
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
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
