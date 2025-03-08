package com.onewhohears.minigames.common.network.toclient;

import com.onewhohears.minigames.util.UtilClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ToClientOpenKitGUI {
    private final String selected;
    private final String[] kits;
    public ToClientOpenKitGUI(String selected, String... kits) {
        this.selected = selected;
        this.kits = kits;
    }
    public ToClientOpenKitGUI(FriendlyByteBuf buffer) {
        selected = buffer.readUtf();
        int length = buffer.readInt();
        kits = new String[length];
        for (int i = 0; i < length; ++i) {
            kits[i] = buffer.readUtf();
        }
    }
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(selected);
        buffer.writeInt(kits.length);
        for (String kit : kits) {
            buffer.writeUtf(kit);
        }
    }
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            ctx.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    success.set(true);
                    UtilClientPacket.handleOpenKitGui(selected, kits);
                });
            });
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
