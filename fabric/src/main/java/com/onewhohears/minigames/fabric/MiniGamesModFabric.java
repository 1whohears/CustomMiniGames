package com.onewhohears.minigames.fabric;

import com.onewhohears.minigames.MiniGamesMod;
import com.onewhohears.minigames.init.MiniGameItems;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;

public class MiniGamesModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MiniGamesMod.init();
        itemGroups();
        if (Platform.getEnvironment() == Env.CLIENT) {
            MiniGamesMod.clientInit();
        }
    }

    public static void itemGroups() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> {
                    entries.accept(MiniGameItems.MONEY.get());
                    entries.accept(MiniGameItems.EVIL_MONEY.get());
                    entries.accept(MiniGameItems.MONEY_MONEY.get());
                    entries.accept(MiniGameItems.WACKY_MONEY.get());
                    entries.accept(MiniGameItems.YAKUZA_MONEY.get());
                    entries.accept(MiniGameItems.ZELDA_MONEY.get());
                });
    }
}
