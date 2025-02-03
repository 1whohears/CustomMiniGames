package com.onewhohears.minigames.client.event;

import com.onewhohears.minigames.MiniGamesMod;
import com.onewhohears.minigames.init.MiniGameEntities;
import com.onewhohears.onewholibs.client.model.obj.ObjEntityModel;
import com.onewhohears.onewholibs.client.renderer.RendererObjEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MiniGamesMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MiniGameEntities.FLAG.get(), context -> new RendererObjEntity<>(
                context, new ObjEntityModel<>("minigames_flag")
        ));
    }

}
