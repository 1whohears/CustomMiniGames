package com.onewhohears.minigames.client.event;

import com.onewhohears.minigames.client.screen.ShopScreen;
import com.onewhohears.minigames.init.MiniGameContainers;
import com.onewhohears.minigames.init.MiniGameEntities;
import com.onewhohears.onewholibs.client.model.obj.ObjEntityModel;
import com.onewhohears.onewholibs.client.renderer.RendererObjEntity;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.Minecraft;

public class MGClientEventHandlers {

    public static void init() {
        ClientLifecycleEvent.CLIENT_SETUP.register(MGClientEventHandlers::clientSetup);
        registerEntityRenderers();
    }

    private static void clientSetup(Minecraft minecraft) {
        MenuRegistry.registerScreenFactory(MiniGameContainers.SHOP_MENU.get(), ShopScreen::new);
    }

    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(MiniGameEntities.FLAG.get(), context -> new RendererObjEntity<>(
                context, new ObjEntityModel<>("computa_flag")
        ));
    }

}
