package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.value.impl.DragValue;
import org.lwjgl.input.Keyboard;

import java.awt.*;

@ModuleInfo(name = "module.combat.keybinds.name", description = "module.combat.keybinds.description", category = Category.RENDER)
public class KeyBinds extends Module {
    private final DragValue position = new DragValue("", this, new Vector2d(200, 100), true);
    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        double x = this.position.position.x;
        double y = this.position.position.y;
        int index = 0;
        for (Module module : Client.INSTANCE.getModuleManager()) {
            if (module.getKeyCode() == Keyboard.KEY_NONE)
                continue;
            index++;
        }

    };
}