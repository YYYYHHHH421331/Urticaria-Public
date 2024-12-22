package cn.hackedmc.urticaria.module.impl.movement.longjump;

import cn.hackedmc.urticaria.component.impl.player.BlinkComponent;
import cn.hackedmc.urticaria.module.impl.movement.LongJump;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.value.Mode;

/**
 * @author Alan, (Nicklas Implemtentet cause yes)
 * @since 08.04.2022
 */
public class MatrixLongJump extends Mode<LongJump> {

    private double lastMotion;
    private int ticks;

    public MatrixLongJump(String name, LongJump parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (!InstanceAccess.mc.thePlayer.onGround)
            ticks++;
        if (InstanceAccess.mc.thePlayer.onGround)
            InstanceAccess.mc.thePlayer.jump();
        if (ticks % 12 == 0 || InstanceAccess.mc.thePlayer.isCollidedVertically)
            lastMotion = InstanceAccess.mc.thePlayer.motionY;
        InstanceAccess.mc.thePlayer.motionY = lastMotion;
        if (InstanceAccess.mc.thePlayer.motionY < 0.1)
            getModule(LongJump.class).setEnabled(false);
    };

    @Override
    public void onEnable() {
        this.ticks = 0;
        this.lastMotion = 0;
        BlinkComponent.blinking = true;
    }

    @Override
    public void onDisable() {
        BlinkComponent.blinking = false;
    }
}