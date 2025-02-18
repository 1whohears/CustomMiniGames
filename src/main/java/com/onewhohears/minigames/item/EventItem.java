package com.onewhohears.minigames.item;

import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.onewhohears.minigames.minigame.data.MiniGameData.RED;

public class EventItem extends Item {

    public EventItem(Properties props) {
        super(props);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                           @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getLevel().isClientSide())
            return InteractionResultHolder.pass(stack);
        if (stack.getTag() == null)
            return sendError(player, stack, "This event item has no data!");
        String event = stack.getTag().getString("event");
        if (event.isEmpty())
            return sendError(player, stack, "This event item has no event id!");
        CompoundTag params = stack.getTag().getCompound("params");
        if (MiniGameManager.get().onEventItemUse((ServerPlayer) player, event, params) &&
                MiniGameManager.get().onEventItemUse((ServerPlayer) player, event, params)) {
            stack.shrink(1);
        }
        return InteractionResultHolder.success(stack);
    }

    private InteractionResultHolder<ItemStack> sendError(Player player, ItemStack stack, String msg) {
        player.sendSystemMessage(UtilMCText.literal(msg).setStyle(RED));
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        if (stack.getTag() == null) return UtilMCText.translatable("event.minigames.not_registered");
        String event = stack.getTag().getString("event");
        if (event.isEmpty()) return UtilMCText.translatable("event.minigames.not_registered");
        return UtilMCText.translatable("event.minigames."+event);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tips, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tips, isAdvanced);
        if (stack.getTag() == null || !stack.getTag().contains("params")) return;
        CompoundTag params = stack.getTag().getCompound("params");
        for (String key : params.getAllKeys()) {
            tips.add(UtilMCText.literal(key+": "+params.get(key)));
        }
    }
}
