package com.onewhohears.minigames.fabric;

import com.onewhohears.minigames.MiniGamesMod;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class MiniGamesDataGenEntrypoint implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        MiniGamesMod.registerDataGens(generator);
    }
}
