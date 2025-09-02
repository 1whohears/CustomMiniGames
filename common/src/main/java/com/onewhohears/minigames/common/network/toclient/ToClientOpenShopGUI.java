package com.onewhohears.minigames.common.network.toclient;

import com.onewhohears.minigames.util.UtilClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ToClientOpenShopGUI {
    private final String[] shops;
    public ToClientOpenShopGUI(String... shops) {
        this.shops = shops;
    }
    public ToClientOpenShopGUI(FriendlyByteBuf buffer) {
        int length = buffer.readInt();
        shops = new String[length];
        for (int i = 0; i < length; ++i) {
            shops[i] = buffer.readUtf();
        }
    }
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(shops.length);
        for (String kit : shops) {
            buffer.writeUtf(kit);
        }
    }
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            ctx.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    success.set(true);
                    UtilClientPacket.handleOpenShopGui(shops);
                });
            });
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
