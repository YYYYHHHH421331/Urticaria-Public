package cn.hackedmc.urticaria.module.impl.player.nofall;

import cn.hackedmc.urticaria.component.impl.player.BlinkComponent;
import cn.hackedmc.urticaria.component.impl.player.FallDistanceComponent;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.MoveMathEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.module.impl.player.NoFall;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * @author LvZiQiao
 * @since 25.1.2024
 */
public class WatchdogNoFall extends Mode<NoFall> {

    public WatchdogNoFall(String name, NoFall parent) {
        super(name, parent);
    }

    private boolean resetTimer = false;

    @Override
    public void onEnable() {
        resetTimer = false;
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (FallDistanceComponent.distance >= 2.5) {
            mc.timer.timerSpeed = 0.5f;
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
            FallDistanceComponent.distance = 0;
            resetTimer = true;
        } else if (resetTimer) {
            mc.timer.timerSpeed = 1f;
        }
    };
}