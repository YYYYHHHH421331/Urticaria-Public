package cn.hackedmc.urticaria.module.impl.player.scaffold.sprint;

import cn.hackedmc.urticaria.component.impl.player.RotationComponent;
import cn.hackedmc.urticaria.module.impl.player.Scaffold;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.util.MathHelper;

public class LegitSprint extends Mode<Scaffold> {

    public LegitSprint(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (Math.abs(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) - MathHelper.wrapAngleTo180_float(RotationComponent.rotations.x)) > 90) {
            mc.gameSettings.keyBindSprint.setPressed(false);
            mc.thePlayer.setSprinting(false);
        }
    };
}
