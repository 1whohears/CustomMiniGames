package com.onewhohears.minigames.data.shops;

import java.io.IOException;

import com.onewhohears.minigames.MiniGamesMod;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;

public class MiniGameShopsGenerator implements DataProvider {
	
	public static void register(DataGenerator generator) {
		generator.addProvider(true, new MiniGameShopsGenerator(generator));
	}
	
	protected final DataGenerator.PathProvider pathProvider;
	
	protected MiniGameShopsGenerator(DataGenerator generator) {
        this.pathProvider = generator.createPathProvider(DataGenerator.Target.DATA_PACK, MiniGameShopsManager.KIND);
    }
	
	@Override
	public void run(CachedOutput cache) throws IOException {
		
	}

	@Override
	public String getName() {
		return MiniGamesMod.MODID+":"+MiniGameShopsManager.KIND;
	}

}
