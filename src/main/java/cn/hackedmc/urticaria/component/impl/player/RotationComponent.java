package cn.hackedmc.urticaria.component.impl.player;

import cn.hackedmc.urticaria.component.Component;
import cn.hackedmc.urticaria.component.impl.player.rotationcomponent.MovementFix;
import cn.hackedmc.urticaria.module.impl.movement.TargetStrafe;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.input.MoveInputEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.JumpEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.StrafeEvent;
import cn.hackedmc.urticaria.newevent.impl.render.LookEvent;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.util.rotation.RotationUtil;
import cn.hackedmc.urticaria.util.vector.Vector2f;

public final class RotationComponent extends Component {
    public static boolean active, smoothed;
    public static Vector2f
            rotations = new Vector2f(0, 0),
            lastRotations = new Vector2f(0, 0),
            targetRotations = new Vector2f(0, 0),
            lastServerRotations = new Vector2f(0, 0);
    private static double rotationSpeed;
    private static MovementFix correctMovement;

    /*
     * This method must be called on Pre Update Event to work correctly
     */
    public static void setRotations(final Vector2f rotations, final double rotationSpeed, final MovementFix correctMovement) {
        RotationComponent.targetRotations = rotations;
        RotationComponent.rotationSpeed = rotationSpeed * 18;
        RotationComponent.correctMovement = correctMovement;
        active = true;

        smooth();
    }

    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (!active || rotations == null || lastRotations == null || targetRotations == null || lastServerRotations == null) {
            rotations = lastRotations = targetRotations = lastServerRotations = new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        }

        if (active) {
            smooth();
        }

//        mc.thePlayer.rotationYaw = rotations.x;
//        mc.thePlayer.rotationPitch = rotations.y;

        if (correctMovement == MovementFix.BACKWARDS_SPRINT && active) {
            if (Math.abs(rotations.x - Math.toDegrees(MoveUtil.direction())) > 45) {
                mc.gameSettings.keyBindSprint.setPressed(false);
                mc.thePlayer.setSprinting(false);
            }
        }
    };


    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<MoveInputEvent> onMove = event -> {
        if (active && rotations != null) {
            if (TargetStrafe.INSTANCE.runGrimAC()) {
                MoveUtil.forwardMovement(event);
            } else if (correctMovement == MovementFix.NORMAL) {
                /*
                 * Calculating movement fix
                 */
                final float yaw = rotations.x;
                MoveUtil.fixMovement(event, yaw);
            }
        }
    };

    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<LookEvent> onLook = event -> {
        if (active && rotations != null) {
            event.setRotation(rotations);
        }
    };

    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (active && (correctMovement == MovementFix.NORMAL || correctMovement == MovementFix.TRADITIONAL) && rotations != null) {
            event.setYaw(rotations.x);
        }
    };

    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<JumpEvent> onJump = event -> {
        if (active && (correctMovement == MovementFix.NORMAL || correctMovement == MovementFix.TRADITIONAL || correctMovement == MovementFix.BACKWARDS_SPRINT) && rotations != null) {
            event.setYaw(rotations.x);
        }
    };

    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (active && rotations != null) {
            final float yaw = rotations.x;
            final float pitch = rotations.y;

            event.setYaw(yaw);
            event.setPitch(pitch);

//            mc.thePlayer.rotationYaw = yaw;
//            mc.thePlayer.rotationPitch = pitch;

            mc.thePlayer.renderYawOffset = yaw;
            mc.thePlayer.rotationYawHead = yaw;
            mc.thePlayer.renderPitchHead = pitch;

            lastServerRotations = new Vector2f(yaw, pitch);

            if (Math.abs((rotations.x - mc.thePlayer.rotationYaw) % 360) < 1 && Math.abs((rotations.y - mc.thePlayer.rotationPitch)) < 1) {
                active = false;

                this.correctDisabledRotations();
            }

            lastRotations = rotations;
        } else {
            lastRotations = new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        }

        targetRotations = new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        smoothed = false;
    };

    private void correctDisabledRotations() {
        final Vector2f rotations = new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        final Vector2f fixedRotations = RotationUtil.resetRotation(RotationUtil.applySensitivityPatch(rotations, lastRotations));

        mc.thePlayer.rotationYaw = fixedRotations.x;
        mc.thePlayer.rotationPitch = fixedRotations.y;
    }

    public static void smooth() {
        if (!smoothed) {
            final float lastYaw = lastRotations.x;
            final float lastPitch = lastRotations.y;
            final float targetYaw = targetRotations.x;
            final float targetPitch = targetRotations.y;

            rotations = RotationUtil.smooth(new Vector2f(lastYaw, lastPitch), new Vector2f(targetYaw, targetPitch),
                    rotationSpeed + Math.random());

            if (correctMovement == MovementFix.NORMAL || correctMovement == MovementFix.TRADITIONAL) {
                mc.thePlayer.movementYaw = rotations.x;
            }

            mc.thePlayer.velocityYaw = rotations.x;
        }

        smoothed = true;

        /*
         * Updating MouseOver
         */
        mc.entityRenderer.getMouseOver(1);
    }
}