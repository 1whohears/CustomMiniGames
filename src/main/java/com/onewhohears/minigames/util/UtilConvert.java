package com.onewhohears.minigames.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class UtilConvert {
	
	public static Vec3 toVec3(BlockPos pos) {
		return new Vec3(pos.getX(), pos.getY(), pos.getZ());
	}
	
}
