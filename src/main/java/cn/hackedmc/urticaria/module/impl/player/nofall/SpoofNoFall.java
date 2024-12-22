package cn.hackedmc.urticaria.module.impl.player.nofall;

import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.module.impl.player.NoFall;
import cn.hackedmc.urticaria.value.Mode;

/**
 * @author Auth
 * @since 3/02/2022
 */
public class SpoofNoFall extends Mode<NoFall> {

    public SpoofNoFall(String name, NoFall parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        event.setOnGround(true);

    };
}