package com.onewhohears.minigames.entity;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FlagEntity extends LivingEntity {
	
	public static final List<ItemStack> EMPTY_LIST = Collections.emptyList();
	
	public FlagEntity(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
		this.blocksBuilding = true;
		this.noPhysics = true;
	}
	
	@Override
	public boolean isAffectedByPotions() {
		return false;
	}

	@Override
	public Iterable<ItemStack> getArmorSlots() {
		return EMPTY_LIST;
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
		
	}

	@Override
	public HumanoidArm getMainArm() {
		return HumanoidArm.RIGHT;
	}

}
