package cn.hackedmc.urticaria.module.impl.player.antivoid;

import cn.hackedmc.urticaria.component.impl.player.FallDistanceComponent;
import cn.hackedmc.urticaria.module.impl.player.AntiVoid;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.NumberValue;

public class CollisionAntiVoid extends Mode<AntiVoid> {

    private final NumberValue distance = new NumberValue("Distance", this, 5, 0, 10, 1);

    public CollisionAntiVoid(String name, AntiVoid parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (FallDistanceComponent.distance > distance.getValue().intValue() && !PlayerUtil.isBlockUnder() && mc.thePlayer.posY + mc.thePlayer.motionY < Math.floor(mc.thePlayer.posY)) {
            mc.thePlayer.motionY = Math.floor(mc.thePlayer.posY) - mc.thePlayer.posY;
            if (mc.thePlayer.motionY == 0) {
                mc.thePlayer.onGround = true;
                event.setOnGround(true);
            }
        }
    };
}