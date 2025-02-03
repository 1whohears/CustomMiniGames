package com.onewhohears.minigames.common.event;

import com.onewhohears.minigames.MiniGamesMod;
import com.onewhohears.minigames.entity.FlagEntity;
import com.onewhohears.minigames.init.MiniGameEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MiniGamesMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(MiniGameEntities.FLAG.get(), FlagEntity.createAttributes().build());
    }

}
