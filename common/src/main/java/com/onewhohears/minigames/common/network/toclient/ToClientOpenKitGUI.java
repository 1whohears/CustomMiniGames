package com.onewhohears.minigames.common.network.toclient;

import com.onewhohears.minigames.util.UtilClientPacket;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;

public class ToClientOpenKitGUI extends BaseS2CMessage {
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
    @Override
    public MessageType getType() {
        return null;
    }
    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(selected);
        buffer.writeInt(kits.length);
        for (String kit : kits) {
            buffer.writeUtf(kit);
        }
    }
    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> UtilClientPacket.handleOpenKitGui(selected, kits));
    }
}
