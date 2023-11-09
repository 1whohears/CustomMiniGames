package com.onewhohears.minigames.common.network.toclient;

import java.util.function.Supplier;

import com.onewhohears.minigames.data.shops.MiniGameShopsManager;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class ToClientDataPackSynch {
	
	public ToClientDataPackSynch() {
	}
	
	public ToClientDataPackSynch(FriendlyByteBuf buffer) {
		MiniGameShopsManager.get().readBuffer(buffer);
	}
	
	public void encode(FriendlyByteBuf buffer) {
		MiniGameShopsManager.get().writeToBuffer(buffer);
	}
	
	public boolean handle(Supplier<Context> ctx) {
		ctx.get().setPacketHandled(true);
		return true;
	}

}
