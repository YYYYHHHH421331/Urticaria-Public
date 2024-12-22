package cn.hackedmc.urticaria.module.impl.player.nofall;

import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.module.impl.player.NoFall;
import cn.hackedmc.urticaria.value.Mode;

/**
 * @author Alan
 * @since 3/02/2022
 */
public class NoGroundNoFall extends Mode<NoFall> {

    public NoGroundNoFall(String name, NoFall parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        event.setOnGround(false);
        event.setPosY(event.getPosY() + Math.random() / 100000000000000000000f);
    };
}