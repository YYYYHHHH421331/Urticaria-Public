package cn.hackedmc.urticaria.module.impl.movement.step;

import cn.hackedmc.urticaria.module.impl.movement.Step;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.StepEvent;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;

/**
 * @author Auth
 * @since 22/3/2022
 */

public class VanillaStep extends Mode<Step> {

    private final NumberValue height = new NumberValue("Height", this, 1, 1, 10, 0.1);
    private final BooleanValue reverse = new BooleanValue("Reverse", this, false);
    private final NumberValue timer = new NumberValue("Timer", this, 0.5, 0.1, 1, 0.1);

    public VanillaStep(String name, Step parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        mc.thePlayer.stepHeight = this.height.getValue().floatValue();

        if (!reverse.getValue() || !PlayerUtil.isBlockUnder(height.getValue().floatValue() + mc.thePlayer.getEyeHeight()) || PlayerUtil.inLiquid()) {
            return;
        }

        if (mc.thePlayer.posY < mc.thePlayer.lastGroundY) {
            if (!mc.thePlayer.onGround && mc.thePlayer.offGroundTicks <= 1) {
                mc.thePlayer.motionY = -height.getValue().doubleValue();
            }
        }

        if (mc.thePlayer.offGroundTicks == 1 && mc.thePlayer.posY < mc.thePlayer.lastLastGroundY) {
            mc.timer.timerSpeed = timer.getValue().floatValue();
        }
    };

    @Override
    public void onDisable() {
        mc.thePlayer.stepHeight = 0.6F;
    }

    @EventLink()
    public final Listener<StepEvent> onStep = event -> {

        if (event.getHeight() > 0.6) {
            mc.timer.timerSpeed = timer.getValue().floatValue();
        }
    };
}