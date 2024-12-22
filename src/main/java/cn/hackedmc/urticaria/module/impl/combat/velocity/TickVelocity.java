package cn.hackedmc.urticaria.module.impl.combat.velocity;


import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.module.impl.combat.Velocity;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.NumberValue;

public final class TickVelocity extends Mode<Velocity>  {

    private final NumberValue tickVelocity = new NumberValue("Tick Velocity", this, 1, 1, 6, 1);

    public TickVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (getParent().onSwing.getValue() || getParent().onSprint.getValue() && !mc.thePlayer.isSwingInProgress) return;

        if (mc.thePlayer.hurtTime == 10 - tickVelocity.getValue().intValue()) {
            MoveUtil.stop();
        }
    };
}
