package cn.hackedmc.urticaria.module.impl.player.nofall;

import cn.hackedmc.urticaria.component.impl.player.FallDistanceComponent;
import cn.hackedmc.urticaria.component.impl.player.RotationComponent;
import cn.hackedmc.urticaria.component.impl.player.rotationcomponent.MovementFix;
import cn.hackedmc.urticaria.module.impl.player.NoFall;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.LivingUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.MoveMathEvent;
import cn.hackedmc.urticaria.newevent.impl.other.WorldChangeEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.util.vector.Vector2f;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public class LagNoFall extends Mode<NoFall> {
    public LagNoFall(String name, NoFall parent) {
        super(name, parent);
    }

    private boolean needLag = false;

    @Override
    public void onEnable() {
        needLag = false;
    }

    @EventLink
    private final Listener<WorldChangeEvent> onWorldChange = event -> {
        needLag = false;
    };

    @EventLink
    private final Listener<MoveMathEvent> onMoveMath = event -> {
        if (mc.thePlayer.positionUpdateTicks < 20 && needLag)
            event.setCancelled();
    };

    @EventLink
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (FallDistanceComponent.distance >= 2.5 && event.getPosX() == mc.thePlayer.lastReportedPosX && event.getPosY() == mc.thePlayer.lastReportedPosY && event.getPosZ() == mc.thePlayer.lastReportedPosZ) {
            event.setOnGround(true);
            FallDistanceComponent.distance = 0;
        }
    };

    @EventLink
    private final Listener<LivingUpdateEvent> onLivingUpdate = event -> {
        if (FallDistanceComponent.distance >= 2.5) {
            needLag = true;
        }

        if (mc.thePlayer.onGround) {
            needLag = false;
        }
    };

    @EventLink
    private final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof S08PacketPlayerPosLook)
            needLag = false;

        if (packet instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity wrapped = (S12PacketEntityVelocity) packet;

            if (wrapped.getEntityID() == mc.thePlayer.getEntityId())
                needLag = false;
        }
    };
}
