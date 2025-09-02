package com.onewhohears.minigames;

import com.onewhohears.minigames.client.event.MGClientEventHandlers;
import com.onewhohears.minigames.common.event.MGCommonEventHandlers;
import com.onewhohears.minigames.common.network.PacketHandler;
import com.onewhohears.minigames.data.kits.MiniGameKitsGenerator;
import com.onewhohears.minigames.data.shops.MiniGameShopsGenerator;
import com.onewhohears.minigames.init.CMGTags;
import com.onewhohears.minigames.init.MiniGameContainers;
import com.onewhohears.minigames.init.MiniGameEntities;
import com.onewhohears.minigames.init.MiniGameItems;
import com.onewhohears.minigames.minigame.MiniGameManager;

import com.onewhohears.minigames.minigame.param.MiniGameParamTypes;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.data.DataGenerator;

public class MiniGamesMod {
	
	public static final String MOD_ID = "minigames";

    public static boolean XAERO_MINIMAP_LOADED = false;
    public static boolean XAERO_MINIMAP_FAIRPLAY_LOADED = false;

    public static boolean isXaeroMinimapLoaded() {
        return XAERO_MINIMAP_LOADED || XAERO_MINIMAP_FAIRPLAY_LOADED;
    }

    public static void init() {
        XAERO_MINIMAP_LOADED = Platform.isModLoaded("xaerominimap");
        XAERO_MINIMAP_FAIRPLAY_LOADED = Platform.isModLoaded("xaerominimapfair");
        CMGTags.init();
        MiniGameContainers.register();
        MiniGameEntities.register();
        MiniGameItems.register();
        MGCommonEventHandlers.init();
        PacketHandler.register();
        MiniGameParamTypes.registerGameParamTypes();
        MiniGameManager.registerGames();
        MiniGameManager.registerItemEvents();
        MiniGameManager.registerPOIGens();
        MiniGameManager.registerGameAgentGens();
        if (Platform.getEnvironment() == Env.CLIENT) {
            MGClientEventHandlers.init();
        }
    }

    public static void registerDataGens(DataGenerator generator) {
        MiniGameKitsGenerator.register(generator);
        MiniGameShopsGenerator.register(generator);
    }
	
}
