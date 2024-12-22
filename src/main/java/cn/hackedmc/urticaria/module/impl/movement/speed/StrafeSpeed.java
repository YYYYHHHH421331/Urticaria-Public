package cn.hackedmc.urticaria.module.impl.movement.speed;

import cn.hackedmc.urticaria.module.impl.movement.Speed;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.input.MoveInputEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.StrafeEvent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;

public final class StrafeSpeed extends Mode<Speed> {

    private final BooleanValue hurtBoost = new BooleanValue("Hurt Boost", this, false);
    private final NumberValue boostSpeed = new NumberValue("Boost Speed", this, 1, 0.1, 9.5, 0.1);

    public StrafeSpeed(String name, Speed parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<StrafeEvent> onStrafe = event -> {

        if (!MoveUtil.isMoving()) {
            MoveUtil.stop();
            return;
        }

        if (InstanceAccess.mc.thePlayer.onGround) {
            InstanceAccess.mc.thePlayer.jump();
        }

        if (hurtBoost.getValue() && InstanceAccess.mc.thePlayer.hurtTime == 9) {
            MoveUtil.strafe(boostSpeed.getValue().doubleValue());
        }

        MoveUtil.strafe();
    };

    @EventLink()
    public final Listener<MoveInputEvent> onMove = event -> {
        event.setJump(false);
    };
}
