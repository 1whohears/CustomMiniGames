package com.onewhohears.minigames.minigame.param;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class IntParamType extends MiniGameParamType<Integer> {

    private final int min, max;
    private final String setterArgumentName;

    public IntParamType(@NotNull String id, @NotNull Integer defaultValue, int min, int max) {
        this(id, defaultValue, min, max, "integer");
    }

    public IntParamType(@NotNull String id, @NotNull Integer defaultValue, int min, int max, String setterArgumentName) {
        super(id, defaultValue);
        this.min = min;
        this.max = max;
        this.setterArgumentName = setterArgumentName;
    }

    @Override
    public void save(CompoundTag tag, Integer value) {
        tag.putInt(getId(), value);
    }

    @Override
    public Integer load(CompoundTag tag) {
        return tag.getInt(getId());
    }

    @Override
    protected String getSetterArgumentName() {
        return setterArgumentName;
    }

    @Override
    protected ArgumentType<Integer> getSetterArgumentType() {
        return IntegerArgumentType.integer(min, max);
    }

    @Override
    protected Integer getInputtedValue(CommandContext<CommandSourceStack> context, String name) {
        return IntegerArgumentType.getInteger(context, name);
    }
}
