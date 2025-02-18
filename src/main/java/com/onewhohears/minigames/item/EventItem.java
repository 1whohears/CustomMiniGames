package com.onewhohears.minigames.item;

import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

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
}
