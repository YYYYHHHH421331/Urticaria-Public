package cn.hackedmc.urticaria.module.impl.movement;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.combat.KillAura;
import cn.hackedmc.urticaria.module.impl.player.Scaffold;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.JumpEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.StrafeEvent;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.util.rotation.RotationUtil;
import cn.hackedmc.urticaria.util.vector.Vector3d;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

import java.util.List;

/**
 * @author Alan
 * @since 20/10/2021
 */
@Rise
@ModuleInfo(name = "module.movement.targetstrafe.name", description = "module.movement.targetstrafe.description", category = Category.MOVEMENT)
public class TargetStrafe extends Module {
    public static TargetStrafe INSTANCE;
    public final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Legit"))
            .add(new SubMode("GrimAC"))
            .setDefault("Legit");
    private final NumberValue range = new NumberValue("Range", this, 1, 0.2, 6, 0.1);

    public final BooleanValue holdJump = new BooleanValue("Hold Jump", this, true);
    private float yaw;
    private Entity target;
    private boolean left, colliding;
    private boolean active;
    public boolean causeSpeed;

    public boolean runGrimAC() {
        return this.isEnabled() && mode.getValue().getName().equalsIgnoreCase("GrimAC") && Speed.INSTANCE.isEnabled() && causeSpeed;
    }

    @EventLink(value = Priorities.HIGH)
    public final Listener<JumpEvent> onJump = event -> {
        if (mode.getValue().getName().equalsIgnoreCase("legit") && target != null && active) {
            event.setYaw(yaw);
        }
    };

    @EventLink(value = Priorities.HIGH)
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (mode.getValue().getName().equalsIgnoreCase("legit") && target != null && active) {
            event.setYaw(yaw);
        }
    };

    @EventLink(value = Priorities.HIGH)
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (mode.getValue().getName().equalsIgnoreCase("legit")) {
            // Disable if scaffold is enabled
            Module scaffold = getModule(Scaffold.class);
            Module killaura = getModule(KillAura.class);

            if (scaffold == null || scaffold.isEnabled() || killaura == null || !killaura.isEnabled()) {
                active = false;
                return;
            }

            active = true;

            /*
             * Getting targets and selecting the nearest one
             */
            Module speed = getModule(Speed.class);
            Module flight = getModule(Flight.class);

            if (holdJump.getValue() && !mc.gameSettings.keyBindJump.isKeyDown() || !(mc.gameSettings.keyBindForward.isKeyDown() &&
                    ((flight != null && flight.isEnabled()) || ((speed != null && speed.isEnabled()))))) {
                target = null;
                return;
            }

            final List<Entity> targets = Client.INSTANCE.getTargetManager().getTargets(this.range.getValue().doubleValue() + 3);

            if (targets.isEmpty()) {
                target = null;
                return;
            }

            if (mc.thePlayer.isCollidedHorizontally || !PlayerUtil.isBlockUnder(5, false)) {
                if (!colliding) {
                    MoveUtil.strafe();
                    left = !left;
                }
                colliding = true;
            } else {
                colliding = false;
            }

            target = targets.get(0);

            if (target == null) {
                return;
            }

            float yaw = RotationUtil.calculate(target).getX() + (90 + 45) * (left ? -1 : 1);

            final double range = this.range.getValue().doubleValue();
            final double posX = -MathHelper.sin((float) Math.toRadians(yaw)) * range + target.posX;
            final double posZ = MathHelper.cos((float) Math.toRadians(yaw)) * range + target.posZ;

            yaw = RotationUtil.calculate(new Vector3d(posX, target.posY, posZ)).getX();

            this.yaw = yaw;
            mc.thePlayer.movementYaw = this.yaw;
        }
    };

    public TargetStrafe() {
        INSTANCE = this;
    }
}