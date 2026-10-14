package com.teyvatarchons.client;

import org.lwjgl.glfw.GLFW;

import com.teyvatarchons.Figure;
import com.teyvatarchons.Spell;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Radial spell wheel: ten buttons arranged in an ellipse. Click one (or press 1-9 / 0) to pick the spell. */
public class WheelScreen extends Screen {
    private final Figure figure;

    public WheelScreen(Figure figure) {
        super(Component.literal(figure.display));
        this.figure = figure;
    }

    @Override
    protected void init() {
        int cx = this.width / 2, cy = this.height / 2;
        double rx = Math.min(this.width * 0.30, 260), ry = Math.min(this.height * 0.36, 150);
        Spell[] spells = figure.spells();
        int bw = 170, bh = 20;
        for (int i = 0; i < spells.length; i++) {
            double a = -Math.PI / 2 + i * Math.PI * 2 / spells.length;
            int x = (int) (cx + Math.cos(a) * rx - bw / 2.0);
            int y = (int) (cy + Math.sin(a) * ry - bh / 2.0);
            final int idx = i;
            this.addRenderableWidget(Button.builder(Component.literal((i + 1) + ". " + spells[i].name()), b -> choose(idx))
                    .bounds(x, y, bw, bh).build());
        }
    }

    private void choose(int idx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.connection.sendCommand("archons select " + (idx + 1));
        this.onClose();
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_9) {
            choose(key - GLFW.GLFW_KEY_1);
            return true;
        }
        if (key == GLFW.GLFW_KEY_0) {
            choose(9);
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        g.drawCenteredString(this.font, figure.display, this.width / 2, this.height / 2 - 14, 0xFF000000 | figure.rgb);
        g.drawCenteredString(this.font, figure.title, this.width / 2, this.height / 2 - 2, 0xFFAAAAAA);
        g.drawCenteredString(this.font, "Pick a spell", this.width / 2, this.height / 2 + 10, 0xFF777777);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
