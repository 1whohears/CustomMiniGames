package com.onewhohears.minigames.data.kits;

import java.io.IOException;

import com.onewhohears.minigames.MiniGamesMod;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;

public class MiniGameKitsGenerator implements DataProvider {
	
	public static void register(DataGenerator generator) {
		generator.addProvider(true, new MiniGameKitsGenerator(generator));
	}
	
	protected final DataGenerator.PathProvider pathProvider;
	
	protected MiniGameKitsGenerator(DataGenerator generator) {
        this.pathProvider = generator.createPathProvider(DataGenerator.Target.DATA_PACK, MiniGameKitsManager.KIND);
    }
	
	@Override
	public void run(CachedOutput cache) throws IOException {
		
	}

	@Override
	public String getName() {
		return MiniGamesMod.MODID+":"+MiniGameKitsManager.KIND;
	}

}
