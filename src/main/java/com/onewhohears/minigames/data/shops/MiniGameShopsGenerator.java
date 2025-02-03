package com.onewhohears.minigames.data.shops;

import com.google.gson.JsonObject;
import com.onewhohears.minigames.MiniGamesMod;

import com.onewhohears.onewholibs.data.jsonpreset.JsonPresetGenerator;
import net.minecraft.data.DataGenerator;

public class MiniGameShopsGenerator extends JsonPresetGenerator<GameShop> {

	public static void register(DataGenerator generator) {
		generator.addProvider(true, new MiniGameShopsGenerator(generator));
	}

	public MiniGameShopsGenerator(DataGenerator output) {
		super(output, MiniGameShopsManager.KIND);
	}

	@Override
	public String getName() {
		return MiniGamesMod.MODID+":"+MiniGameShopsManager.KIND;
	}

	@Override
	protected void registerPresets() {
		JsonObject healthPotionNbt = new JsonObject();
		healthPotionNbt.addProperty("Potion", "minecraft:strong_healing");
		addPresetToGenerate(GameShop.Builder.create(MiniGamesMod.MODID, "survival")
				.addProduct("minecraft:bread", 8, "minigames:money", 1)
				.addProduct("minecraft:cooked_beef", 5, "minigames:money", 1)
				.addProduct("minecraft:arrow", 8, "minigames:money", 1)
				.addProduct("minecraft:bow", "minigames:money", 2)
				.addProduct("minecraft:crossbow", "minigames:money", 2)
				.addProduct("minecraft:shield", true, "minigames:money", 4)
				.addProduct("minecraft:splash_potion", healthPotionNbt, "minigames:money", 4)
				.build());
	}
}
