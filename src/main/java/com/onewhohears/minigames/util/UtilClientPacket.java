package com.onewhohears.minigames.util;

import com.onewhohears.minigames.client.screen.GameSelectionScreen;
import com.onewhohears.minigames.client.screen.KitSelectionScreen;
import com.onewhohears.minigames.client.screen.ShopSelectionScreen;
import net.minecraft.client.Minecraft;

import java.util.Map;

public class UtilClientPacket {

    public static void handleOpenKitGui(String selected, String... kits) {
        Minecraft m = Minecraft.getInstance();
        m.setScreen(new KitSelectionScreen(selected, kits));
    }

    public static void handleOpenShopGui(String[] shops) {
        Minecraft m = Minecraft.getInstance();
        m.setScreen(new ShopSelectionScreen(shops));
    }

    public static void handleGameSelectGui(String[] ids, Map<String, String[]> teamMap) {
        Minecraft m = Minecraft.getInstance();
        m.setScreen(new GameSelectionScreen(ids, teamMap));
    }
}
