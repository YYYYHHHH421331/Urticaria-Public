package cn.hackedmc.urticaria.component.impl.patches;

import cn.hackedmc.urticaria.component.Component;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public class GuiClosePatchComponent extends Component {

    private boolean inGUI;

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (mc.currentScreen == null && inGUI) {
            for (final KeyBinding bind : mc.gameSettings.keyBindings) {
                bind.setPressed(GameSettings.isKeyDown(bind));
            }
        }

        inGUI = mc.currentScreen != null;
    };
}
