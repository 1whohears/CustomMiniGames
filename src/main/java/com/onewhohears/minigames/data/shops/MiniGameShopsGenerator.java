package com.onewhohears.minigames.data.shops;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.onewhohears.minigames.MiniGamesMod;
import com.onewhohears.minigames.data.JsonData;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

public class MiniGameShopsGenerator implements DataProvider {
	
	public static void register(DataGenerator generator) {
		generator.addProvider(true, new MiniGameShopsGenerator(generator));
	}
	
	protected final DataGenerator.PathProvider pathProvider;
	private final Map<ResourceLocation, JsonData> gen_map = new HashMap<>();
	
	protected MiniGameShopsGenerator(DataGenerator generator) {
        this.pathProvider = generator.createPathProvider(DataGenerator.Target.DATA_PACK, MiniGameShopsManager.KIND);
    }
	
	protected void registerShops() {
		
	}
	
	protected void registerShop(JsonData shop) {
		gen_map.put(shop.getKey(), shop);
	}
	
	@Override
	public void run(CachedOutput cache) throws IOException {
		gen_map.clear();
		registerShops();
		Set<ResourceLocation> set = Sets.newHashSet();
		gen_map.forEach((key, shop) -> {
			if (!set.add(shop.getKey())) {
				throw new IllegalStateException("Duplicate Shop Not Allowed! " + shop.getKey());
			} else {
				Path path = pathProvider.json(shop.getKey());
				try {
					DataProvider.saveStable(cache, shop.getJsonData(), path);
				} catch (IOException e) {
					e.printStackTrace();
	            }
			}
		});
	}

	@Override
	public String getName() {
		return MiniGamesMod.MODID+":"+MiniGameShopsManager.KIND;
	}

}
