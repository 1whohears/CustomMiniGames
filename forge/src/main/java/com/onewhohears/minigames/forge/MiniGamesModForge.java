package com.onewhohears.minigames.forge;

import com.onewhohears.minigames.MiniGamesMod;
import dev.architectury.platform.Platform;
import dev.architectury.platform.forge.EventBuses;
import dev.architectury.utils.Env;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MiniGamesMod.MOD_ID)
public class MiniGamesModForge {

    // Leave this here for now otherwise we will be incompatible w/ older versions of Forge
    public MiniGamesModForge() {
        @SuppressWarnings("removal")
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(MiniGamesMod.MOD_ID, modEventBus);

        modEventBus.addListener(this::onGatherData);

        MiniGamesMod.init();
        if (Platform.getEnvironment() == Env.CLIENT && !DatagenModLoader.isRunningDataGen()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> MiniGamesMod::clientInit);
        }
    }

    // Compatible with newer versions of Forge
    public MiniGamesModForge(FMLJavaModLoadingContext loadingContext) {
        IEventBus modEventBus = loadingContext.getModEventBus();
        EventBuses.registerModEventBus(MiniGamesMod.MOD_ID, modEventBus);

        modEventBus.addListener(this::onGatherData);

        MiniGamesMod.init();
        if (Platform.getEnvironment() == Env.CLIENT && !DatagenModLoader.isRunningDataGen()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> MiniGamesMod::clientInit);
        }
    }

    private void onGatherData(GatherDataEvent event) {
        if (event.includeServer()) {
            MiniGamesMod.registerServerDataGens(event.getGenerator());
        }
    }

}
