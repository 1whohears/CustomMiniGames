package com.onewhohears.minigames.common.network;

import com.onewhohears.minigames.MiniGamesMod;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
	
public final class PacketHandler {
	
	private PacketHandler() {}
	
	private static final String PROTOCOL_VERSION = "1.0";
	
	public static SimpleChannel INSTANCE;

	public static void register() {
		SimpleChannel net = NetworkRegistry.ChannelBuilder
				.named(new ResourceLocation(MiniGamesMod.MODID, "messages"))
				.networkProtocolVersion(() -> PROTOCOL_VERSION)
				.clientAcceptedVersions(s -> s.equals(PROTOCOL_VERSION))
				.serverAcceptedVersions(s -> s.equals(PROTOCOL_VERSION))
				.simpleChannel();
		INSTANCE = net;
		int index = 0;
	}
	
}
