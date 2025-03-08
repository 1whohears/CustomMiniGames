package com.onewhohears.minigames.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.onewhohears.minigames.common.network.PacketHandler;
import com.onewhohears.minigames.common.network.toserver.ToServerKitSelect;
import com.onewhohears.minigames.data.kits.GameKit;
import com.onewhohears.minigames.data.kits.MiniGameKitsManager;
import com.onewhohears.onewholibs.client.screen.BackgroundScreen;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KitSelectionScreen extends BackgroundScreen {

    public static final ResourceLocation BG = new ResourceLocation("minigames:textures/gui/basic_bg.png");

    private final String[] kits;
    private final Button[] selectButtons;
    private String selected = "";

    public KitSelectionScreen(String selected, String... kits) {
        super("Kit Selection", BG, 256, 180, 256, 256);
        this.selected = selected;
        this.kits = kits;
        this.selectButtons = new Button[kits.length];
    }

    @Override
    protected void init() {
        this.vertical_widget_shift = 10;
        super.init();
        for (int i = 0; i < kits.length; ++i) {
            selectButtons[i] = createSelectButton(kits[i]);
            positionWidgetGrid(selectButtons[i], 9, 6, i*6, 2);
        }
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        font.draw(poseStack, UtilMCText.literal("Kits are applied in the next round!"),
                guiX + left_padding, guiY + top_padding,0x0000AA);
        for (int i = 0; i < kits.length; ++i) {
            Button button = selectButtons[i];
            GameKit kit = MiniGameKitsManager.get().get(kits[i]);
            if (kit == null) continue;
            int x = button.x + 40, y = button.y + 6;
            int color = 0x444444;
            if (kit.getId().equals(selected)) color = 0x0000AA;
            font.draw(poseStack, kit.getDisplayNameComponent(), x, y, color);
            List<ItemStack> stacks = kit.getItemsForDisplay();
            int num = stacks.size();
            int cycle = (int) ((System.currentTimeMillis() / 750) % num);
            for (int j = 0; j < num && j < 6; ++j) {
                int k = j;
                if (num >= 6) {
                    k += cycle;
                    if (k >= num) k -= num;
                }
                getMinecraft().getItemRenderer().renderGuiItem(stacks.get(k), x + 90 + j * 20, button.y);
            }
        }
    }

    private Button createSelectButton(String kit) {
        return new Button(0, 0, 20, 20, UtilMCText.literal("Select"), getSelectOnPress(kit));
    }

    private Button.OnPress getSelectOnPress(String kit) {
        return button -> {
            PacketHandler.INSTANCE.sendToServer(new ToServerKitSelect(kit));
            selected = kit;
        };
    }
}
