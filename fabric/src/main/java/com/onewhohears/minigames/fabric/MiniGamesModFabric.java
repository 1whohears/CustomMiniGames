package com.onewhohears.minigames.fabric;

import com.onewhohears.minigames.MiniGamesMod;
import net.fabricmc.api.ModInitializer;

public class MiniGamesModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MiniGamesMod.init();
    }
}
