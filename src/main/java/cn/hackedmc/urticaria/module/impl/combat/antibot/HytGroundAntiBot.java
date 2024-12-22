package cn.hackedmc.urticaria.module.impl.combat.antibot;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.module.impl.combat.AntiBot;
import cn.hackedmc.urticaria.module.impl.combat.KillAura;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S14PacketEntity;


public final class HytGroundAntiBot extends Mode<AntiBot> {
    public HytGroundAntiBot(String name, AntiBot parent) {
        super(name, parent);
    }

    @EventLink
    private final Listener<PreUpdateEvent> onPreMotionEvent = event -> {
        mc.theWorld.playerEntities.forEach(player -> {
            if ((player.isPlayerSleeping() || !player.isEntityAlive() || player.getEyeHeight() <= 0.5f) && player.hurtTime == 0) {
                Client.INSTANCE.getBotManager().add(player);
            } else {
                Client.INSTANCE.getBotManager().remove(player);
            }
        });
    };

    @EventLink()
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        Packet<?> packet = event.getPacket();
        if(packet instanceof S14PacketEntity){
            S14PacketEntity packetEntity = (S14PacketEntity) packet;
            Entity entity = packetEntity.getEntity(mc.theWorld);
            if (entity instanceof EntityPlayer && entity.isDead) {
                if (packetEntity.onGround && !Client.INSTANCE.getBotManager().contains(entity)){
                    Client.INSTANCE.getBotManager().add(entity);
                }
            }
        }
    };
}

