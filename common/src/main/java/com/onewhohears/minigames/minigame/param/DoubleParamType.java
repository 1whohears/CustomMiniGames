package com.onewhohears.minigames.minigame.param;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class DoubleParamType extends MiniGameParamType<Double> {

    private final double min, max;

    public DoubleParamType(@NotNull String id, @NotNull Double defaultValue, double min, double max) {
        super(id, defaultValue);
        this.min = min;
        this.max = max;
    }

    @Override
    public void save(CompoundTag tag, Double value) {
        tag.putDouble(getId(), value);
    }

    @Override
    public Double load(CompoundTag tag) {
        return tag.getDouble(getId());
    }

    @Override
    protected String getSetterArgumentName() {
        return "number";
    }

    @Override
    protected ArgumentType<Double> getSetterArgumentType() {
        return DoubleArgumentType.doubleArg(min, max);
    }

    @Override
    protected Double getInputtedValue(CommandContext<CommandSourceStack> context, String name) {
        return DoubleArgumentType.getDouble(context, name);
    }
}
