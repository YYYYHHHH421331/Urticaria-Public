package cn.hackedmc.urticaria.component.impl.player;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.Component;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;

@Rise
public final class FallDistanceComponent extends Component {

    public static float distance;
    private float lastDistance;

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        final float fallDistance = mc.thePlayer.fallDistance;

        if (fallDistance == 0) {
            distance = 0;
        }

        distance += fallDistance - lastDistance;
        lastDistance = fallDistance;
    };
}
