package com.onewhohears.minigames.init;

import com.google.common.collect.ImmutableSet;
import com.onewhohears.minigames.MiniGamesMod;

import com.onewhohears.minigames.entity.FlagEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class MiniGameEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
            MiniGamesMod.MOD_ID, Registry.ENTITY_TYPE_REGISTRY);
	
	public static final RegistrySupplier<EntityType<FlagEntity>> FLAG = ENTITY_TYPES.register("flag",
			() -> createEntityType(FlagEntity::new, EntityDimensions.scalable(0.8f, 2)));
	
	public static <T extends Entity> EntityType<T> createEntityType(EntityType.EntityFactory<T> factory, EntityDimensions size) {
        return new EntityType<>(factory, MobCategory.MISC, true, true, true,
        		true, ImmutableSet.of(), size, 8, 3);
    }

    public static void register() {
        ENTITY_TYPES.register();
    }
	
}
