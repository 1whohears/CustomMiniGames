package com.onewhohears.minigames.init;

import com.onewhohears.minigames.MiniGamesMod;

import com.onewhohears.minigames.item.EventItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public class MiniGameItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            MiniGamesMod.MOD_ID, Registries.ITEM);

	public static final RegistrySupplier<EventItem> EVENT_ITEM = ITEMS.register("event",
			() -> new EventItem(new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> MONEY = ITEMS.register("money",
			() -> new Item(new Item.Properties().stacksTo(64).food(
					new FoodProperties.Builder().nutrition(-1).alwaysEat()
						.build())));
	
	public static final RegistrySupplier<Item> EVIL_MONEY = ITEMS.register("evil_money",
			() -> new Item(new Item.Properties().stacksTo(64).food(
					new FoodProperties.Builder().nutrition(-20).alwaysEat()
						.effect(new MobEffectInstance(MobEffects.WITHER, 400, 5), 1)
						.build())));
	
	public static final RegistrySupplier<Item> MONEY_MONEY = ITEMS.register("money_money",
			() -> new Item(new Item.Properties().stacksTo(64).food(
					new FoodProperties.Builder().nutrition(-20).alwaysEat()
						.effect(new MobEffectInstance(MobEffects.HARM, 1, 100), 1)
						.build())));
	
	public static final RegistrySupplier<Item> WACKY_MONEY = ITEMS.register("wacky_money",
			() -> new Item(new Item.Properties().stacksTo(64).food(
					new FoodProperties.Builder().nutrition(-5).alwaysEat()
						.effect(new MobEffectInstance(MobEffects.POISON, 400, 5), 1)
						.build())));
	
	public static final RegistrySupplier<Item> ZELDA_MONEY = ITEMS.register("zelda_money",
			() -> new Item(new Item.Properties().stacksTo(64).food(
					new FoodProperties.Builder().nutrition(-5).alwaysEat()
						.effect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 5), 1)
						.build())));
	
	public static final RegistrySupplier<Item> YAKUZA_MONEY = ITEMS.register("yakuza_money",
			() -> new Item(new Item.Properties().stacksTo(64).food(
					new FoodProperties.Builder().nutrition(-5).alwaysEat()
						.effect(new MobEffectInstance(MobEffects.CONFUSION, 600, 10), 1)
						.build())));

    public static void register() {
        ITEMS.register();
    }
}
