package cn.hackedmc.urticaria.component.impl.player;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.Component;
import cn.hackedmc.urticaria.component.impl.render.SmoothCameraComponent;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.input.MoveInputEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import lombok.Getter;

@Rise
public final class PacketlessDamageComponent extends Component {

    @Getter
    private static boolean active;
    private static float timer;
    private static int jumps;

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (active) {
            if (jumps < 3) {
                mc.timer.timerSpeed = timer;

                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                    jumps++;
                }

                event.setOnGround(false);
            } else if (mc.thePlayer.onGround) {
                mc.timer.timerSpeed = 1.0F;
                active = false;
                timer = 1.0F;
                jumps = 0;
            }

            SmoothCameraComponent.setY();
        }
    };

    @EventLink()
    public final Listener<MoveInputEvent> onMove = event -> {
        if (active) {
            event.setForward(0);
            event.setStrafe(0);
        }
    };

    public static void setActive(final float timer) {
        PacketlessDamageComponent.active = true;
        PacketlessDamageComponent.timer = timer;
        jumps = 0;
    }
}
