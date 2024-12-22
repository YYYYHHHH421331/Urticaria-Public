package cn.hackedmc.urticaria.module.impl.movement.flight;

import cn.hackedmc.urticaria.module.impl.movement.Flight;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.other.TickEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.Vec3;

public class GrimACVertical extends Mode<Flight> {
    private int ticks;
    private boolean exploited = false;

    public GrimACVertical(String name, Flight parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        ticks = 0;
        exploited = false;
        mc.timer.timerSpeed = 1F;
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1.0F;
        ticks = 0;
        exploited = false;
    }

    @EventLink
    private final Listener<PreMotionEvent> onPreUpdate = event -> {
        if (ticks >= 40) return;

        if (mc.thePlayer.fallDistance > 2F) {
            this.toggle();
        }
        if (ticks == 0) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
            }
        } else if (ticks <= 5) {
            mc.timer.timerSpeed = 0.45F;
        } else {
            mc.timer.timerSpeed = 1F;
        }
        ticks++;
        if (exploited || ticks == 2) {
            exploited = false;
            PacketUtil.sendNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(
                    mc.thePlayer.posX + 114514, -10, mc.thePlayer.posZ + 1919180, false));
        }
        if (ticks > 2) {
            event.setCancelled();
        }
    };

    @EventLink
    private final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof S12PacketEntityVelocity && ((S12PacketEntityVelocity) packet).getEntityID() == mc.thePlayer.getEntityId()) {
            S12PacketEntityVelocity velocity = (S12PacketEntityVelocity) event.getPacket();
            double str = new Vec3(velocity.getMotionX(), 0, velocity.getMotionZ()).lengthVector();
            if (str > 1) {
                this.getParent().setEnabled(false);
            }
        }
        if (packet instanceof S08PacketPlayerPosLook) {
            exploited = true;
        }
    };
}
