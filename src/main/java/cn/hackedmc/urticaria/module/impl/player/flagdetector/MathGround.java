package cn.hackedmc.urticaria.module.impl.player.flagdetector;

import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.module.impl.player.FlagDetector;
import cn.hackedmc.urticaria.value.Mode;


public class MathGround extends Mode<FlagDetector> {

    public MathGround(String name, FlagDetector parent) {
        super(name, parent);
    }

    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (event.isOnGround() && event.getPosY() % (1 / 64f) != 0) {
            getParent().fail("Math Ground");
        }

    };
}
