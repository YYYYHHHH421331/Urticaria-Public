package cn.hackedmc.urticaria.module.impl.render.interfaces;

import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public final class ModuleComponent {

    public Module module;
    public float offsetX = 0;
    public float offsetY = 0;
    public Vector2d position = new Vector2d(5000, 0), targetPosition = new Vector2d(5000, 0);
    public float animationTime;
    public String tag = "";
    public float nameWidth = 0, tagWidth;
    public Color color = Color.WHITE;
    public String translatedName = "";
    public ModuleComponent(final Module module) {
        this.module = module;
        offsetX = 0;
    }
}