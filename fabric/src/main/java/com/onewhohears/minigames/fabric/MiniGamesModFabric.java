package com.onewhohears.minigames.fabric;

import com.onewhohears.minigames.MiniGamesMod;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.ModInitializer;

public class MiniGamesModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MiniGamesMod.init();
        if (Platform.getEnvironment() == Env.CLIENT) {
            MiniGamesMod.clientInit();
        }
    }
}
