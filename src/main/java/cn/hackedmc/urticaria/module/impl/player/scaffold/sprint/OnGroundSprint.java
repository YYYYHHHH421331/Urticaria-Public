package cn.hackedmc.urticaria.module.impl.player.scaffold.sprint;

import cn.hackedmc.urticaria.module.impl.player.Scaffold;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.client.settings.GameSettings;

public class OnGroundSprint extends Mode<Scaffold> {
    public OnGroundSprint(String name, Scaffold parent) {
        super(name, parent);
    }

    @Override
    public void onDisable() {
        mc.gameSettings.keyBindSprint.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindSprint));
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        mc.gameSettings.keyBindSprint.setPressed(mc.thePlayer.onGround && MoveUtil.enoughMovementForSprinting());
    };
}
