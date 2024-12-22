package cn.hackedmc.urticaria.module.impl.movement.step;

import cn.hackedmc.urticaria.module.impl.movement.Step;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.potion.Potion;

/**
 * @author Alan
 * @since 22/3/2022
 */

public class NCPPacketLessStep extends Mode<Step> {

    private boolean step;

    public NCPPacketLessStep(String name, Step parent) {
        super(name, parent);
    }

    @Override
    public void onDisable() {
        mc.thePlayer.stepHeight = 0.6F;
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (mc.thePlayer.onGround && mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isPotionActive(Potion.jump)) {
            mc.thePlayer.jump();
            MoveUtil.stop();
            step = true;
        }

        if (mc.thePlayer.offGroundTicks == 3 && step) {
            mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 2);
            MoveUtil.strafe(MoveUtil.WALK_SPEED);
            step = false;
        }
    };
}