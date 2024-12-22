package cn.hackedmc.urticaria.module.impl.movement.flight;

import cn.hackedmc.urticaria.module.impl.movement.Flight;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.input.MoveInputEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.StrafeEvent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.NumberValue;

/**
 * @author Auth
 * @since 18/11/2021
 */

public class VanillaFlight extends Mode<Flight> {

    private final NumberValue speed = new NumberValue("Speed", this, 1, 0.1, 9.5, 0.1);

    public VanillaFlight(String name, Flight parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<StrafeEvent> onStrafe = event -> {

        final float speed = this.speed.getValue().floatValue();

        event.setSpeed(speed);
    };


    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        final float speed = this.speed.getValue().floatValue();

        InstanceAccess.mc.thePlayer.motionY = 0.0D
                + (InstanceAccess.mc.gameSettings.keyBindJump.isKeyDown() ? speed : 0.0D)
                - (InstanceAccess.mc.gameSettings.keyBindSneak.isKeyDown() ? speed : 0.0D);
    };

    @EventLink()
    public final Listener<MoveInputEvent> onMove = event -> {
        event.setSneak(false);
    };

    @Override
    public void onDisable() {
        MoveUtil.stop();
    }
}