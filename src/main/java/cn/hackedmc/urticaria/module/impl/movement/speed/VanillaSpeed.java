package cn.hackedmc.urticaria.module.impl.movement.speed;

import cn.hackedmc.urticaria.module.impl.movement.Speed;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.StrafeEvent;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.NumberValue;

/**
 * @author Auth
 * @since 18/11/2021
 */

public class VanillaSpeed extends Mode<Speed> {

    private final NumberValue speed = new NumberValue("Speed", this, 1, 0.1, 9.5, 0.1);

    public VanillaSpeed(String name, Speed parent) {
        super(name, parent);
    }


    @EventLink()
    public final Listener<StrafeEvent> onStrafe = event -> {

        if (MoveUtil.isMoving() && mc.thePlayer.onGround) {
            mc.thePlayer.jump();
        }

        event.setSpeed(speed.getValue().floatValue());
    };

    @Override
    public void onDisable() {

    }
}