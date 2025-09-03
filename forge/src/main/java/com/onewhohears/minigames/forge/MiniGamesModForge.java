package com.onewhohears.minigames.forge;

import com.onewhohears.minigames.MiniGamesMod;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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
    }

    // Compatible with newer versions of Forge
    public MiniGamesModForge(FMLJavaModLoadingContext loadingContext) {
        IEventBus modEventBus = loadingContext.getModEventBus();
        EventBuses.registerModEventBus(MiniGamesMod.MOD_ID, modEventBus);

        modEventBus.addListener(this::onGatherData);

        MiniGamesMod.init();
    }

    private void onGatherData(GatherDataEvent event) {
        if (event.includeServer()) {
            MiniGamesMod.registerServerDataGens(event.getGenerator());
        }
    }

    @Mod.EventBusSubscriber(modid = MiniGamesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            MiniGamesMod.clientInit();
        }
    }

}
