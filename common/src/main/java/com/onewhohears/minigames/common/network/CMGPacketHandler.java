package com.onewhohears.minigames.common.network;

import com.onewhohears.minigames.MiniGamesMod;

import com.onewhohears.minigames.common.network.toclient.ToClientGameJoinGUI;
import com.onewhohears.minigames.common.network.toclient.ToClientOpenKitGUI;
import com.onewhohears.minigames.common.network.toclient.ToClientOpenShopGUI;
import com.onewhohears.minigames.common.network.toserver.ToServerGameSelect;
import com.onewhohears.minigames.common.network.toserver.ToServerKitSelect;
import com.onewhohears.minigames.common.network.toserver.ToServerShopSelect;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
	
public final class CMGPacketHandler {
	
	private CMGPacketHandler() {}

    public static final SimpleNetworkManager INSTANCE = SimpleNetworkManager.create(MiniGamesMod.MOD_ID);

    public static final MessageType S2C_GAME_JOIN_GUI = INSTANCE.registerS2C(
            "game_join_gui", ToClientGameJoinGUI::new);
    public static final MessageType S2C_KIT_GUI = INSTANCE.registerS2C(
            "kit_gui", ToClientOpenKitGUI::new);
    public static final MessageType S2C_SHOP_GUI = INSTANCE.registerS2C(
            "shop_gui", ToClientOpenShopGUI::new);

    public static final MessageType C2S_SHOP_GUI = INSTANCE.registerC2S(
            "select_game", ToServerGameSelect::new);
    public static final MessageType C2S_KIT = INSTANCE.registerC2S(
            "select_kit", ToServerKitSelect::new);
    public static final MessageType C2S_SHOP = INSTANCE.registerC2S(
            "select_shop", ToServerShopSelect::new);

	public static void register() {}
	
}
