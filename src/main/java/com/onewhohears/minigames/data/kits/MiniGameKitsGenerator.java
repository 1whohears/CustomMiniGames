package com.onewhohears.minigames.data.kits;

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

public class MiniGameKitsGenerator implements DataProvider {
	
	public static void register(DataGenerator generator) {
		generator.addProvider(true, new MiniGameKitsGenerator(generator));
	}
	
	protected final DataGenerator.PathProvider pathProvider;
	private final Map<ResourceLocation, JsonData> gen_map = new HashMap<>();
	
	protected MiniGameKitsGenerator(DataGenerator generator) {
        this.pathProvider = generator.createPathProvider(DataGenerator.Target.DATA_PACK, MiniGameKitsManager.KIND);
    }
	
	protected void registerKits() {
		registerKit(GameKit.Builder.create(MiniGamesMod.MODID, "warrior")
				.addItem("minecraft:iron_sword")
				.build());
	}
	
	protected void registerKit(JsonData kit) {
		gen_map.put(kit.getKey(), kit);
	}
	
	@Override
	public void run(CachedOutput cache) throws IOException {
		gen_map.clear();
		registerKits();
		Set<ResourceLocation> set = Sets.newHashSet();
		gen_map.forEach((key, kit) -> {
			if (!set.add(kit.getKey())) {
				throw new IllegalStateException("Duplicate Kit Not Allowed! " + kit.getKey());
			} else {
				Path path = pathProvider.json(kit.getKey());
				try {
					DataProvider.saveStable(cache, kit.getJsonData(), path);
				} catch (IOException e) {
					e.printStackTrace();
	            }
			}
		});
	}

	@Override
	public String getName() {
		return MiniGamesMod.MODID+":"+MiniGameKitsManager.KIND;
	}

}
