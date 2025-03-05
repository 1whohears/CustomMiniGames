package com.onewhohears.minigames.minigame.param;

import com.onewhohears.minigames.minigame.MiniGameManager;

public final class MiniGameParamTypes {

    public static final BoolParamType CLEAR_ON_START = new BoolParamType("clearOnStart", false);

    /**
     * called in {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}
     * register all built in games param types here
     */
    public static void registerGameParamTypes() {
        MiniGameManager.registerGameParamType(CLEAR_ON_START);
    }
}
