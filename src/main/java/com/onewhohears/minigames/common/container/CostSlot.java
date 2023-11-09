package com.onewhohears.minigames.common.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class CostSlot extends Slot {

	public CostSlot(ShopContainer pContainer, int pSlot, int pX, int pY) {
		super(pContainer, pSlot, pX, pY);
	}
	
	@Override
	public boolean mayPickup(Player player) {
		return false;
	}

}
