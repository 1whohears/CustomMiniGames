package com.onewhohears.minigames.client.screen;

import com.onewhohears.minigames.common.network.PacketHandler;
import com.onewhohears.minigames.common.network.toserver.ToServerShopSelect;
import com.onewhohears.minigames.data.shops.GameShop;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.onewholibs.client.screen.BackgroundScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;

public class ShopSelectionScreen extends BackgroundScreen {

    public static final ResourceLocation BG = new ResourceLocation("minigames:textures/gui/basic_bg.png");

    private final String[] shops;

    public ShopSelectionScreen(String... kits) {
        super("Shop Selection", BG, 256, 180, 256, 256);
        this.shops = kits;
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < shops.length; ++i) {
            GameShop shop = MiniGameShopsManager.get().get(shops[i]);
            if (shop == null) continue;
            positionWidgetGrid(createSelectButton(shop), 9, 1, i, 4);
        }
    }

    private Button createSelectButton(GameShop shop) {
        return new Button(0, 0, 20, 20, shop.getDisplayNameComponent(), getSelectOnPress(shop.getId()));
    }

    private Button.OnPress getSelectOnPress(String shop) {
        return button -> {
            PacketHandler.INSTANCE.sendToServer(new ToServerShopSelect(shop));
        };
    }
}
