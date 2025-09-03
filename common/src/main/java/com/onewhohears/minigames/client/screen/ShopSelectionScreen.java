package com.onewhohears.minigames.client.screen;

import com.onewhohears.minigames.common.network.toserver.ToServerShopSelect;
import com.onewhohears.minigames.data.shops.GameShop;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.onewholibs.client.screen.BackgroundScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;

public class ShopSelectionScreen extends BackgroundScreen {

    public static final ResourceLocation BG = new ResourceLocation("minigames:textures/gui/basic_bg.png");

    private final String[] shops;

    public ShopSelectionScreen(String... shops) {
        super("Shop Selection", BG, 256, 180, 256, 256);
        this.shops = shops;
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
        return Button.builder(shop.getDisplayNameComponent(), getSelectOnPress(shop.getId()))
                .pos(0, 0).size(20, 20).build();
    }

    private Button.OnPress getSelectOnPress(String shop) {
        return button -> new ToServerShopSelect(shop).sendToServer();
    }
}
