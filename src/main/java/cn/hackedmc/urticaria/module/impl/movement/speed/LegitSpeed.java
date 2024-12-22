package cn.hackedmc.urticaria.module.impl.movement.speed;

import cn.hackedmc.urticaria.component.impl.player.BlinkComponent;
import cn.hackedmc.urticaria.component.impl.player.RotationComponent;
import cn.hackedmc.urticaria.component.impl.player.rotationcomponent.MovementFix;
import cn.hackedmc.urticaria.module.impl.movement.Speed;
import cn.hackedmc.urticaria.module.impl.movement.TargetStrafe;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.StrafeEvent;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.util.vector.Vector2f;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;

/**
 * @author Alan
 * @since 18/11/2022
 */

public class LegitSpeed extends Mode<Speed> {

    private final ModeValue rotationExploit = new ModeValue("Rotation Exploit Mode", this)
            .add(new SubMode("Off"))
            .add(new SubMode("1.9+ Bounding box"))
            .add(new SubMode("Rotate (Fully Legit)"))
            .add(new SubMode("Speed Equivalent (Almost legit, Very hard to flag)"))
            .setDefault("Speed Equivalent (Almost legit, Very hard to flag)");
    private final BooleanValue autoJump = new BooleanValue("Auto Jump", this, false, () -> !rotationExploit.getValue().getName().equalsIgnoreCase("1.9+ bounding box"));
    private final BooleanValue cpuSpeedUpExploit = new BooleanValue("CPU SpeedUp Exploit", this, true);
    private final BooleanValue noJumpDelay = new BooleanValue("No Jump Delay", this, true);

    public LegitSpeed(String name, Speed parent) {
        super(name, parent);
    }

    @Override
    public void onDisable() {
        TargetStrafe.INSTANCE.causeSpeed = false;
    }

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<PreUpdateEvent> preUpdate = event -> {
        switch (rotationExploit.getValue().getName()) {
            case "Rotate (Fully Legit)":
                if (!mc.thePlayer.onGround)
                    RotationComponent.setRotations(new Vector2f(mc.thePlayer.rotationYaw + 45, mc.thePlayer.rotationPitch), 10, MovementFix.NORMAL);
                break;

            case "Speed Equivalent (Almost legit, Very hard to flag)":
                MoveUtil.useDiagonalSpeed();
                break;

            case "1.9+ Bounding box":

                break;
        }

        if (noJumpDelay.getValue()) {
            mc.thePlayer.jumpTicks = 0;
        }

        if (cpuSpeedUpExploit.getValue()) {
            mc.timer.timerSpeed = 1.004f;
        }
    };

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<StrafeEvent> strafe = event -> {
        if (!MoveUtil.isMoving()) return;

        if (rotationExploit.getValue().getName().equalsIgnoreCase("1.9+ bounding box")) {
            if (autoJump.getValue() && mc.thePlayer.onGround)
                mc.thePlayer.jump();
        } else {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
            }
        }
    };
}
