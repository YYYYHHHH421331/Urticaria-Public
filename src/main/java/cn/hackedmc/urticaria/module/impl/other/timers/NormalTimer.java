package cn.hackedmc.urticaria.module.impl.other.timers;

import cn.hackedmc.urticaria.module.impl.other.Timer;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.math.MathUtil;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.BoundsNumberValue;

public class NormalTimer extends Mode<Timer> {
    private final BoundsNumberValue speedValue = new BoundsNumberValue("Speed", this, 2, 2, 0.1, 10, 0.1);

    public NormalTimer(String name, Timer parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.timer.timerSpeed = (float) MathUtil.getRandom(speedValue.getValue().floatValue(), speedValue.getSecondValue().floatValue());
    };
}
