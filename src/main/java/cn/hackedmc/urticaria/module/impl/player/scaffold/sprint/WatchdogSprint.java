package cn.hackedmc.urticaria.module.impl.player.scaffold.sprint;

import cn.hackedmc.urticaria.module.impl.player.Scaffold;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.MoveMathEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;

import java.util.ArrayList;
import java.util.List;

public class WatchdogSprint extends Mode<Scaffold> {
    public WatchdogSprint(String name, Scaffold parent) {
        super(name, parent);
    }

    private final List<Packet<?>> packets = new ArrayList<>();

    public void onDisable() {
        if (!packets.isEmpty()) {
            packets.forEach(mc.getNetHandler()::addToSendQueueUnregistered);
            packets.clear();
        }

        mc.thePlayer.motionX *= .8;
        mc.thePlayer.motionZ *= .8;
    }

    @EventLink
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (MoveUtil.getLastDistance() > .22 && mc.thePlayer.ticksExisted % 2 == 0 && mc.thePlayer.onGround) {
            final double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
            final double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
            final double multiplier = .5 - MoveUtil.getSpeedEffect() * .05;
            final double random = Math.random() * .007;
            event.setPosX(event.getPosX() - xDist * (multiplier + random));
            event.setPosZ(event.getPosZ() - zDist * (multiplier + random));
            event.setPosY(event.getPosY() + .00625 + Math.random() * 1E-3);
        }

        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionX *= 1.114 - MoveUtil.getSpeedEffect() * .01 - Math.random() * 1E-4;
            mc.thePlayer.motionZ *= 1.114 - MoveUtil.getSpeedEffect() * .01 - Math.random() * 1E-4;
        }

        if (mc.thePlayer.ticksExisted % 2 != 0 && !packets.isEmpty()) {
            packets.forEach(mc.getNetHandler()::addToSendQueueUnregistered);
            packets.clear();
        }
    };

    @EventLink
    private final Listener<PacketSendEvent> onPacketSend = event -> {
        if (mc.thePlayer.onGround && mc.thePlayer.ticksExisted % 2 == 0
                && (event.getPacket() instanceof C08PacketPlayerBlockPlacement
                || event.getPacket() instanceof C0APacketAnimation
                || event.getPacket() instanceof C09PacketHeldItemChange)) {
            packets.add(event.getPacket());
            event.setCancelled(true);
        }
    };
}
