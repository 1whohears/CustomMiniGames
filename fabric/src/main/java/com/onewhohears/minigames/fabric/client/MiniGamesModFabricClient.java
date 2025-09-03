package com.onewhohears.minigames.fabric.client;

import com.onewhohears.minigames.MiniGamesMod;
import net.fabricmc.api.ClientModInitializer;

public class MiniGamesModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MiniGamesMod.clientInit();
    }
}
