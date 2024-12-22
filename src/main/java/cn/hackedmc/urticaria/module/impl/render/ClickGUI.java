package cn.hackedmc.urticaria.module.impl.render;


import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.input.KeyboardInputEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;

/**
 * Displays a GUI which can display and do various things
 *
 * @author Alan
 * @since 04/11/2021
 */
@ModuleInfo(name = "module.render.clickgui.name", description = "module.render.clickgui.description", category = Category.RENDER, keyBind = Keyboard.KEY_RSHIFT)
public final class ClickGUI extends Module {
    @Override
    public void onEnable() {
        Client.INSTANCE.getEventBus().register(Client.INSTANCE.getStandardClickGUI());
        mc.displayGuiScreen(Client.INSTANCE.getStandardClickGUI());
//        Client.INSTANCE.getStandardClickGUI().overlayPresent = null;
    }

    @Override
    public void onDisable() {
        Keyboard.enableRepeatEvents(false);
        Client.INSTANCE.getEventBus().unregister(Client.INSTANCE.getStandardClickGUI());
        Client.INSTANCE.getExecutor().execute(() -> Client.INSTANCE.getConfigFile().write());
        this.mc.displayGuiScreen(null);
    }

    @EventLink(value = Priorities.HIGH)
    public final Listener<Render2DEvent> onRender2D = event -> {
        double width = event.getScaledResolution().getScaledWidth();
        double height = event.getScaledResolution().getScaledHeight();

        UI_RENDER_RUNNABLES.add(() -> Client.INSTANCE.getStandardClickGUI().render());
        UI_BLOOM_RUNNABLES.add(() -> Client.INSTANCE.getStandardClickGUI().bloom());
        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.rectangle(0, 0, width, height, Color.BLACK));

        if (this.mc.currentScreen == null) {
            this.setEnabled(false);
        }
    };

    @EventLink()
    public final Listener<KeyboardInputEvent> onKey = event -> {
        if (event.getKeyCode() == this.getKeyCode()) {
            if (this.mc.currentScreen == null) {
                this.mc.setIngameFocus();
            }
        }
    };
}
