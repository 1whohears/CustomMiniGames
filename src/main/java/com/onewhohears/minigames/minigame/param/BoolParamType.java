package com.onewhohears.minigames.minigame.param;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class BoolParamType extends MiniGameParamType<Boolean> {

    public BoolParamType(@NotNull String id, @NotNull Boolean defaultValue) {
        super(id, defaultValue);
    }

    @Override
    public void save(CompoundTag tag, Boolean value) {
        tag.putBoolean(getId(), value);
    }

    @Override
    public Boolean load(CompoundTag tag) {
        return tag.getBoolean(getId());
    }

    @Override
    protected String getSetterArgumentName() {
        return "enable";
    }

    @Override
    protected ArgumentType<Boolean> getSetterArgumentType() {
        return BoolArgumentType.bool();
    }

    @Override
    protected Boolean getInputtedValue(CommandContext<CommandSourceStack> context, String name) {
        return BoolArgumentType.getBool(context, name);
    }
}
