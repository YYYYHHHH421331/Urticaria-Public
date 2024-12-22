package cn.hackedmc.urticaria.module.impl.movement.sneak;

import cn.hackedmc.urticaria.module.impl.movement.Sneak;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.value.Mode;

/**
 * @author Auth
 * @since 25/06/2022
 */

public class StandardSneak extends Mode<Sneak> {

    public StandardSneak(String name, Sneak parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        mc.thePlayer.movementInput.sneak = mc.thePlayer.sendQueue.doneLoadingTerrain;
    };
}