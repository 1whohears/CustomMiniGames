package com.onewhohears.minigames.common.network;

import com.onewhohears.minigames.MiniGamesMod;

import com.onewhohears.minigames.common.network.toclient.ToClientOpenKitGUI;
import com.onewhohears.minigames.common.network.toclient.ToClientOpenShopGUI;
import com.onewhohears.minigames.common.network.toserver.ToServerKitSelect;
import com.onewhohears.minigames.common.network.toserver.ToServerShopSelect;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
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
		net.messageBuilder(ToServerKitSelect.class, index++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(ToServerKitSelect::encode)
				.decoder(ToServerKitSelect::new)
				.consumerMainThread(ToServerKitSelect::handle)
				.add();
		net.messageBuilder(ToClientOpenKitGUI.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ToClientOpenKitGUI::encode)
				.decoder(ToClientOpenKitGUI::new)
				.consumerMainThread(ToClientOpenKitGUI::handle)
				.add();
		net.messageBuilder(ToServerShopSelect.class, index++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(ToServerShopSelect::encode)
				.decoder(ToServerShopSelect::new)
				.consumerMainThread(ToServerShopSelect::handle)
				.add();
		net.messageBuilder(ToClientOpenShopGUI.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ToClientOpenShopGUI::encode)
				.decoder(ToClientOpenShopGUI::new)
				.consumerMainThread(ToClientOpenShopGUI::handle)
				.add();
	}
	
}
