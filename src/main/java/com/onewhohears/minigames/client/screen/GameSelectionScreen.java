package com.onewhohears.minigames.client.screen;

import com.onewhohears.minigames.common.network.PacketHandler;
import com.onewhohears.minigames.common.network.toserver.ToServerGameSelect;
import com.onewhohears.minigames.common.network.toserver.ToServerShopSelect;
import com.onewhohears.minigames.data.shops.GameShop;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.onewholibs.client.screen.BackgroundScreen;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class GameSelectionScreen extends BackgroundScreen {

    public static final ResourceLocation BG = new ResourceLocation("minigames:textures/gui/basic_bg.png");

    private final String[] ids;
    private final Map<String, String[]> teamMap;

    public GameSelectionScreen(String[] ids, Map<String, String[]> teamMap) {
        super("Game Selection", BG, 256, 180, 256, 256);
        this.ids = ids;
        this.teamMap = teamMap;
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < ids.length; ++i) {
            positionWidgetGrid(createSelectButton(ids[i]), 9, 1, i, 4);
        }
    }

    private Button createSelectButton(String id) {
        return new Button(0, 0, 20, 20, UtilMCText.literal(id), getSelectOnPress(id));
    }

    private Button.OnPress getSelectOnPress(String id) {
        return button -> getMinecraft().setScreen(new TeamSelectionScreen(ids, teamMap, id));
    }
}
