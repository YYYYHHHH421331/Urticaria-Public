package cn.hackedmc.urticaria.module.impl.movement.wallclimb;

import cn.hackedmc.urticaria.module.impl.movement.WallClimb;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.util.MathHelper;

/**
 * @author Alan
 * @since 22/3/2022
 */

public class VulcanWallClimb extends Mode<WallClimb> {

    public VulcanWallClimb(String name, WallClimb parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (InstanceAccess.mc.thePlayer.isCollidedHorizontally) {
            if (InstanceAccess.mc.thePlayer.ticksExisted % 2 == 0) {
                event.setOnGround(true);
                InstanceAccess.mc.thePlayer.motionY = MoveUtil.jumpMotion();
            }

            final double yaw = MoveUtil.direction();
            event.setPosX(event.getPosX() - -MathHelper.sin((float) yaw) * 0.1f);
            event.setPosZ(event.getPosZ() - MathHelper.cos((float) yaw) * 0.1f);
        }
    };
}