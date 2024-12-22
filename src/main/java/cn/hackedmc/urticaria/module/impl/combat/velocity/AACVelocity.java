package cn.hackedmc.urticaria.module.impl.combat.velocity;

import cn.hackedmc.urticaria.component.impl.player.BadPacketsComponent;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.module.impl.combat.Velocity;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.impl.input.MoveInputEvent;
import cn.hackedmc.urticaria.value.Mode;

public final class AACVelocity extends Mode<Velocity> {

    private boolean jump;

    public AACVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (getParent().onSwing.getValue() || getParent().onSprint.getValue() && !mc.thePlayer.isSwingInProgress) return;

        if (mc.thePlayer.hurtTime > 0 && !BadPacketsComponent.bad(false, true,false,false,false)) {
            mc.thePlayer.motionX *= 0.6D;
            mc.thePlayer.motionZ *= 0.6D;
        }

        jump = false;
    };

    @EventLink()
    public final Listener<MoveInputEvent> onMove = event -> {
        if (getParent().onSwing.getValue() || getParent().onSprint.getValue() && !mc.thePlayer.isSwingInProgress) return;

        if (jump) {
            event.setJump(true);
        }
    };
}
