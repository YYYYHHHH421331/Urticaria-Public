package cn.hackedmc.urticaria.module.impl.movement.step;

import cn.hackedmc.urticaria.module.impl.movement.Step;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.StepEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * @author Alan
 * @since 22/3/2022
 */

public class VulcanStep extends Mode<Step> {

    public VulcanStep(String name, Step parent) {
        super(name, parent);
    }

    @Override
    public void onDisable() {
        mc.thePlayer.stepHeight = 0.6F;
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (mc.thePlayer.ticksSinceJump > 11) {
            mc.thePlayer.stepHeight = 1;
        } else {
            mc.thePlayer.stepHeight = 0.6F;
        }
    };

    @EventLink()
    public final Listener<StepEvent> onStep = event -> {

        if (event.getHeight() > 0.6) {
            mc.timer.timerSpeed = 0.5f;
            PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.5F, mc.thePlayer.posZ, true));
        }
    };
}