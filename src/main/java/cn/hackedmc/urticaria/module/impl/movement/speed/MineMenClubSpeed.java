package cn.hackedmc.urticaria.module.impl.movement.speed;

import cn.hackedmc.urticaria.module.impl.movement.Speed;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.StrafeEvent;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.Mode;

/**
 * @author Alan
 * @since 18/11/2022
 */

public class MineMenClubSpeed extends Mode<Speed> {

    public MineMenClubSpeed(String name, Speed parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<StrafeEvent> onStrafe = event -> {

        if (mc.thePlayer.hurtTime <= 6) {
            MoveUtil.strafe();
        }
    };

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
        }
    };
}