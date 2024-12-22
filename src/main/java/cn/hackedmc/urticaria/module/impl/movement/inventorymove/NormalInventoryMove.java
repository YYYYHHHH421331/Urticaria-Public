package cn.hackedmc.urticaria.module.impl.movement.inventorymove;

import cn.hackedmc.urticaria.module.impl.movement.InventoryMove;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.other.ClientTickEvent;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public final class NormalInventoryMove extends Mode<InventoryMove> {
    public NormalInventoryMove(String name, InventoryMove parent) {
        super(name, parent);
    }

    private static final KeyBinding[] AFFECTED_BINDINGS = new KeyBinding[]{
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindJump
    };


    @EventLink
    private final Listener<ClientTickEvent> preUpdateEventListener = event -> {
        if(mc.currentScreen == null || mc.currentScreen instanceof GuiChat || mc.currentScreen == this.getStandardClickGUI()) return;

        for (final KeyBinding bind : AFFECTED_BINDINGS) {
            bind.setPressed(GameSettings.isKeyDown(bind));
        }
    };
}
