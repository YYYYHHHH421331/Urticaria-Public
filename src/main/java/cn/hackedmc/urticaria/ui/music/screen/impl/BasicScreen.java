package cn.hackedmc.urticaria.ui.music.screen.impl;

import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.vector.Vector2f;

public abstract class BasicScreen implements InstanceAccess {
    protected Vector2f getPosition() {
        return getMusicMenu().position;
    }

    protected Vector2f getScale() {
        return getMusicMenu().scale;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    }
}
