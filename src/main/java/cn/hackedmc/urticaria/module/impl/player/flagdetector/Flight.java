package cn.hackedmc.urticaria.module.impl.player.flagdetector;

import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.module.impl.player.FlagDetector;
import cn.hackedmc.urticaria.value.Mode;


public class Flight extends Mode<FlagDetector> {

    public Flight(String name, FlagDetector parent) {
        super(name, parent);
    }

    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (mc.thePlayer.offGroundTicks <= 1 || mc.thePlayer.ticksSinceVelocity == 1 ||
                mc.thePlayer.isCollidedHorizontally || mc.thePlayer.capabilities.isFlying ||
                Math.abs(mc.thePlayer.motionY - MoveUtil.UNLOADED_CHUNK_MOTION) < 1E-5 || mc.thePlayer.isCollidedVertically ||
                mc.thePlayer.ticksSinceTeleport == 1 || mc.thePlayer.isInWeb) return;

        if (Math.abs((((Math.abs(mc.thePlayer.lastMotionY) < 0.005 ? 0 : mc.thePlayer.lastMotionY) - 0.08) * 0.98F) -
                mc.thePlayer.motionY) > 1E-5) {
            getParent().fail("Flight");
        }

    };
}
