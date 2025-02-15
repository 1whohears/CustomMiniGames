package com.onewhohears.minigames.util;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class CMGUtil {

    public static void forceHeldItemSync(ServerPlayer sp, InteractionHand hand) {
        if (sp.connection != null) {
            if (hand == InteractionHand.MAIN_HAND) {
                sp.connection.send(new ClientboundContainerSetSlotPacket(-2, 0,
                        sp.getInventory().selected, sp.getItemInHand(hand)));
            } /*else if (hand == InteractionHand.OFF_HAND) { // FIXME off-hand hand syncing
                List<Pair<EquipmentSlot, ItemStack>> slots = new ArrayList<>();
                slots.add(new Pair<>(EquipmentSlot.OFFHAND, sp.getInventory().offhand.get(0)));
                sp.connection.send(new ClientboundSetEquipmentPacket(sp.getId(), slots));
            }*/
        }
    }

}
