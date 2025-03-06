package com.onewhohears.minigames.minigame.param;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class FloatParamType extends MiniGameParamType<Float> {

    private final float min, max;

    public FloatParamType(@NotNull String id, @NotNull Float defaultValue, float min, float max) {
        super(id, defaultValue);
        this.min = min;
        this.max = max;
    }

    @Override
    public void save(CompoundTag tag, Float value) {
        tag.putFloat(getId(), value);
    }

    @Override
    public Float load(CompoundTag tag) {
        return tag.getFloat(getId());
    }

    @Override
    protected String getSetterArgumentName() {
        return "number";
    }

    @Override
    protected ArgumentType<Float> getSetterArgumentType() {
        return FloatArgumentType.floatArg(min, max);
    }

    @Override
    protected Float getInputtedValue(CommandContext<CommandSourceStack> context, String name) {
        return FloatArgumentType.getFloat(context, name);
    }
}
