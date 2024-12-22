package cn.hackedmc.urticaria.module.impl.ghost.wtap;

import cn.hackedmc.urticaria.component.impl.player.BlinkComponent;
import cn.hackedmc.urticaria.module.impl.combat.KillAura;
import cn.hackedmc.urticaria.module.impl.ghost.WTap;
import cn.hackedmc.urticaria.module.impl.player.AutoGApple;
import cn.hackedmc.urticaria.module.impl.player.antivoid.FreezeAntiVoid;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.other.MoveMathEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public class PacketWTap extends Mode<WTap> {
    private final NumberValue lagTime = new NumberValue("Rates", this, 15, 10, 20, 1);
    private final NumberValue count = new NumberValue("Times", this, 3, 0, 8, 1);

    public PacketWTap(String name, WTap parent) {
        super(name, parent);
    }

    private boolean skipped = false;
    private boolean lastSave = false;
    private boolean needSkip = false;

    @EventLink
    private final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity wrapped = (S12PacketEntityVelocity) packet;

            if (wrapped.getEntityID() == mc.thePlayer.getEntityId())
                needSkip = true;
        }
    };

    @EventLink
    private final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (KillAura.INSTANCE.target != null && mc.thePlayer.ticksExisted % lagTime.getValue().intValue() <= count.getValue().intValue() && mc.thePlayer.positionUpdateTicks < 20 && !getModule(AutoGApple.class).isEnabled() && !FreezeAntiVoid.running && !needSkip && !BlinkComponent.blinking) {
            if (!skipped) {
                skipped = true;
                lastSave = mc.thePlayer.isSprinting();
            }
            mc.thePlayer.setSprinting(false);
        } else {
            if (skipped) {
                skipped = false;
                mc.thePlayer.setSprinting(lastSave);
            }
        }
    };

    @EventLink
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        needSkip = false;
    };

    @EventLink
    private final Listener<MoveMathEvent> onAttack = event -> {
        if (KillAura.INSTANCE.target != null && mc.thePlayer.ticksExisted % lagTime.getValue().intValue() <= count.getValue().intValue() && mc.thePlayer.positionUpdateTicks < 20 && !getModule(AutoGApple.class).isEnabled() && !FreezeAntiVoid.running && !needSkip && !BlinkComponent.blinking) event.setCancelled();
    };
}
