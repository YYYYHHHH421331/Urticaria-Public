package cn.hackedmc.urticaria.module.impl.combat.criticals;

import cn.hackedmc.urticaria.module.impl.combat.Criticals;
import cn.hackedmc.urticaria.module.impl.combat.KillAura;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;

public class GrimACCriticals extends Mode<Criticals> {
    public GrimACCriticals(String name, Criticals parent) {
        super(name, parent);
    }

    private boolean attacking = false;

    @EventLink
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (KillAura.INSTANCE.target != null && attacking) {
            if (mc.thePlayer.fallDistance > 0 || mc.thePlayer.offGroundTicks > 3) {
                event.setOnGround(false);
            }
        } else {
            attacking = false;
        }
    };

    @EventLink
    private final Listener<PacketSendEvent> onPacketSend = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C02PacketUseEntity) {
            final C02PacketUseEntity wrapped = (C02PacketUseEntity) packet;

            if (wrapped.getAction() == C02PacketUseEntity.Action.ATTACK) {
                attacking = true;
            }
        }
    };
}
