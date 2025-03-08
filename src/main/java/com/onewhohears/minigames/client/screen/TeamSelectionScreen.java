package com.onewhohears.minigames.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.onewhohears.minigames.common.network.PacketHandler;
import com.onewhohears.minigames.common.network.toserver.ToServerGameSelect;
import com.onewhohears.onewholibs.client.screen.BackgroundScreen;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TeamSelectionScreen extends BackgroundScreen {

    public static final ResourceLocation BG = new ResourceLocation("minigames:textures/gui/basic_bg.png");

    private final String[] ids;
    private final Map<String, String[]> teamMap;
    private final String selectedGame;
    private final List<Button> teamButtons = new ArrayList<>();

    public TeamSelectionScreen(String[] ids, Map<String, String[]> teamMap, String selectedGame) {
        super("Game Selection", BG, 256, 180, 256, 256);
        this.ids = ids;
        this.teamMap = teamMap;
        this.selectedGame = selectedGame;
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        ClientPacketListener clientPacketListener = getMinecraft().getConnection();
        if (clientPacketListener == null) return;
        if (!teamMap.containsKey(selectedGame)) return;
        String[] teams = teamMap.get(selectedGame);
        Collection<PlayerInfo> infoCollection = clientPacketListener.getOnlinePlayers();
        if (infoCollection.isEmpty()) return;
        PlayerInfo[] players = infoCollection.toArray(new PlayerInfo[0]);
        for (int i = 0; i < teams.length; ++i) {
            int k = 0, x = teamButtons.get(i).x, y = teamButtons.get(i).y;
            for (PlayerInfo info : players) {
                if (info.getTeam() == null) continue;
                if (!info.getTeam().getName().equals(teams[i])) continue;
                Component name = info.getTabListDisplayName();
                font.draw(poseStack, name != null ? name : UtilMCText.literal(info.getProfile().getName()),
                        x+2, y+22+k*10, 0xFFFFFF);
                ++k;
            }
        }
    }

    @Override
    protected void init() {
        this.vertical_widget_shift = 10;
        super.init();
        positionWidgetGrid(createBackButton(), 9, 2, 2);
        this.vertical_widget_shift = 32;
        if (teamMap.containsKey(selectedGame)) {
            String[] teams = teamMap.get(selectedGame);
            int verticalSpace = 0;
            for (int i = 0; i < teams.length; ++i) {
                Button button = createSelectButton(teams[i]);
                teamButtons.add(button);
                positionWidgetGrid(button, 9, 2, i+verticalSpace);
                if (i % 2 == 1) verticalSpace += 4;
            }
        } else {
            positionWidgetGrid(createSelectButton(""), 0, 1, 9, 2);
        }
    }

    private Button createSelectButton(String id) {
        return new Button(0, 0, 20, 20, UtilMCText.literal(id), getSelectTeamOnPress(id));
    }

    private Button.OnPress getSelectTeamOnPress(String team) {
        return button -> {
            PacketHandler.INSTANCE.sendToServer(new ToServerGameSelect(selectedGame, team));
        };
    }

    private Button createBackButton() {
        return new Button(0, 0, 20, 20, UtilMCText.literal("Back"), getBackOnPress());
    }

    private Button.OnPress getBackOnPress() {
        return button -> getMinecraft().setScreen(new GameSelectionScreen(ids, teamMap));
    }
}
