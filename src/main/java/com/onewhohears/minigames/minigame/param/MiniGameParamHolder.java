package com.onewhohears.minigames.minigame.param;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class MiniGameParamHolder<T extends MiniGameParamType<E>, E> {

    @NotNull private final T type;
    @NotNull private E value;

    public MiniGameParamHolder(@NotNull T type) {
        this.type = type;
        value = type.getDefaultValue();
    }

    public void save(CompoundTag tag) {
        getType().save(tag, get());
    }
    public void load(CompoundTag tag) {
        set(getType().load(tag));
    }

    public @NotNull T getType() {
        return type;
    }

    public @NotNull E get() {
        return value;
    }

    public void set(@NotNull E value) {
        this.value = value;
    }

}
