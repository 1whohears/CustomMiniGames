package com.onewhohears.minigames.minigame.param;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.onewhohears.onewholibs.util.UtilParse;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Vec3ParamType extends MiniGameParamType<Vec3> {

    public Vec3ParamType(@NotNull String id, @NotNull Vec3 defaultValue) {
        super(id, defaultValue);
    }

    @Override
    public void save(CompoundTag tag, Vec3 value) {
        UtilParse.writeVec3(tag, value, getId());
    }

    @Override
    public Vec3 load(CompoundTag tag) {
        return UtilParse.readVec3(tag, getId());
    }

    @Override
    protected String getSetterArgumentName() {
        return "pos";
    }

    @Override
    protected ArgumentType<Coordinates> getSetterArgumentType() {
        return Vec3Argument.vec3();
    }

    @Override
    protected Vec3 getInputtedValue(CommandContext<CommandSourceStack> context, String name) {
        return Vec3Argument.getVec3(context, name);
    }
}
