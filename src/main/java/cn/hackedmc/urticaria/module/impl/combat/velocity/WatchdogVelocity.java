package cn.hackedmc.urticaria.module.impl.combat.velocity;

import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.module.impl.combat.Velocity;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.viamcp.ViaMCP;

public final class WatchdogVelocity extends Mode<Velocity> {
    public WatchdogVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    private final BooleanValue speedLimit = new BooleanValue("Speed Limit",this , true);
    private final NumberValue speedMaxValue = new NumberValue("Max Speed", this, 0.5, 0.1, 1.0, 0.1, () -> !speedLimit.getValue());

    @EventLink()
    public final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        if (getParent().onSwing.getValue() || getParent().onSprint.getValue() && !mc.thePlayer.isSwingInProgress)
            return;

        final Packet<?> p = event.getPacket();

        if (p instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity wrapper = (S12PacketEntityVelocity) p;

            if (wrapper.getEntityID() == mc.thePlayer.getEntityId()) {
                if (wrapper.motionX * wrapper.motionX + wrapper.motionZ + wrapper.motionZ >= 2 * 8000.0 * 2 * 8000.0) return;

                event.setCancelled();

                if (mc.thePlayer.onGround) mc.thePlayer.motionY = wrapper.motionY / 8000.0;
            }
        }
    };
}
