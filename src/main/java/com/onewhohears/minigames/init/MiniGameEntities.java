package com.onewhohears.minigames.init;

import com.google.common.collect.ImmutableSet;
import com.onewhohears.minigames.MiniGamesMod;

import com.onewhohears.minigames.entity.FlagEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MiniGameEntities {
	
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MiniGamesMod.MODID);
	
	public static void register(IEventBus eventBus) {
		ENTITIES.register(eventBus);
	}
	
	public static final RegistryObject<EntityType<FlagEntity>> FLAG = ENTITIES.register("flag",
			() -> createEntityType(FlagEntity::new, EntityDimensions.scalable(0.8f, 2)));
	
	public static <T extends Entity> EntityType<T> createEntityType(EntityType.EntityFactory<T> factory, EntityDimensions size) {
        return new EntityType<>(factory, MobCategory.MISC, true, true, true,
        		true, ImmutableSet.of(), size, 8, 3);
    }
	
}
