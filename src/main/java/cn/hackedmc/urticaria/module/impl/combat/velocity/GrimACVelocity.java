package cn.hackedmc.urticaria.module.impl.combat.velocity;

import cn.hackedmc.urticaria.component.impl.player.RotationComponent;
import cn.hackedmc.urticaria.module.impl.combat.KillAura;
import cn.hackedmc.urticaria.module.impl.combat.Velocity;
import cn.hackedmc.urticaria.module.impl.player.antivoid.FreezeAntiVoid;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.LivingUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.MoveMathEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.util.chat.ChatUtil;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.*;

import java.util.LinkedList;
import java.util.Queue;

public class GrimACVelocity extends Mode<Velocity> {
    public GrimACVelocity(String name, Velocity parent) {
        super(name, parent);
    }
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Block Spoof"))
            .add(new SubMode("Attack Reduce"))
            .add(new SubMode("Attack Reduce (Legit Sprint)"))
            .add(new SubMode("Attack Reduce (Lower Hit)"))
            .add(new SubMode("HYT BedWars With AAC"))
            .add(new SubMode("1.17+"))
            .setDefault("Block Spoof");
    private final BooleanValue grimAttackedFlightObjectValue = new BooleanValue("GrimAC Attack Flight Object",this,false);
    private final NumberValue grimBestMotion = new NumberValue("Target Motion", this, 0.1, 0.01, 1, 0.001);
    private final BooleanValue debug = new BooleanValue("Debug", this, false);
    private boolean needUpdate;
    private double reduceXZ;

    @Override
    public void onEnable() {
        reduceXZ = 0;
        needUpdate = false;
    }

    @EventLink
    private final Listener<MoveMathEvent> onMoveMath = event -> {
        if (mode.getValue().getName().equalsIgnoreCase("HYT BedWars With AAC") && needUpdate && mc.thePlayer.hurtTime == 9)
            event.setCancelled();
    };
    @EventLink(value = Priorities.VERY_LOW)
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mode.getValue().getName().equalsIgnoreCase("1.17+")) {
            mc.getNetHandler().addToSendQueueUnregistered(new C03PacketPlayer.C06PacketPlayerPosLook(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    RotationComponent.rotations.x,
                    RotationComponent.rotations.y,
                    mc.thePlayer.onGround
            ));
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.thePlayer), EnumFacing.UP));
            event.setCancelled();
        }
    };

    @EventLink(value = Priorities.VERY_LOW)
    private final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        if (mc.thePlayer == null || event.isCancelled()) return;
        Packet<?> packet = event.getPacket();

        if (packet instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity wrapped = (S12PacketEntityVelocity) packet;

            if (wrapped.getEntityID() == mc.thePlayer.getEntityId()) {
                switch (mode.getValue().getName().toLowerCase()) {
                    case "block spoof": {
                        mc.getNetHandler().addToSendQueue(new C03PacketPlayer(mc.thePlayer.onGround));
                        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.thePlayer), EnumFacing.UP));
                        mc.timer.lastSyncSysClock += 1;
                        event.setCancelled();

                        break;
                    }
                    case "attack reduce (lower hit)": {
                        if (KillAura.INSTANCE.target != null && !FreezeAntiVoid.running) {
                            needUpdate = true;

                            boolean state = EntityPlayerSP.serverSprintState;

                            reduceXZ = 1;

                            if (!state) {
                                mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                            }

                            final double motionX = wrapped.motionX / 8000.0;
                            final double motionZ = wrapped.motionZ / 8000.0;
                            double velocityDistance = Math.sqrt(motionX * motionX + motionZ * motionZ);

                            int counter = 0;
                            while (velocityDistance * reduceXZ > grimBestMotion.getValue().floatValue() && counter < 8) {
                                mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(KillAura.INSTANCE.target, C02PacketUseEntity.Action.ATTACK));
                                mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                                reduceXZ *= 0.6;
                                counter++;
                            }

                            if (debug.getValue()) {
                                ChatUtil.display("Reduce velocity: %.3f -> %.3f (Attack %d times)", velocityDistance, velocityDistance * reduceXZ, counter);
                            }

                            if (!state) {
                                mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                            }
                        } else if (grimAttackedFlightObjectValue.getValue()) {
                            for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                                if (entity instanceof IProjectile && entity != mc.thePlayer) {
                                    if (entity.onGround || mc.thePlayer.getDistanceToEntity(entity) > 6) continue;

                                    needUpdate = true;

                                    boolean state = EntityPlayerSP.serverSprintState;

                                    reduceXZ = 1;

                                    if (!state) {
                                        mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                                    }

                                    final double motionX = wrapped.motionX / 8000.0;
                                    final double motionZ = wrapped.motionZ / 8000.0;
                                    double velocityDistance = Math.sqrt(motionX * motionX + motionZ * motionZ);

                                    int counter = 0;
                                    while (velocityDistance * reduceXZ > grimBestMotion.getValue().floatValue() && counter < 8) {
                                        mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK));
                                        mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                                        reduceXZ *= 0.6;
                                        counter++;
                                    }

                                    if (debug.getValue()) {
                                        ChatUtil.display("Reduce velocity: %.3f -> %.3f (Attack %d times)", velocityDistance, velocityDistance * reduceXZ, counter);
                                    }

                                    if (!state) {
                                        mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                                    }
                                    break;
                                }
                            }
                        }

                        break;
                    }
                    case "hyt bedwars with aac":
                    case "attack reduce (legit sprint)": {
                        if (KillAura.INSTANCE.target != null && !FreezeAntiVoid.running) {
                            needUpdate = true;

                            boolean state = EntityPlayerSP.serverSprintState;

                            if (!state) {
                                mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                            }

                            for (int i = 0; i < 8; i++) {
                                mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(KillAura.INSTANCE.target, C02PacketUseEntity.Action.ATTACK));
                                mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                            }

                            reduceXZ = getMotion();

                            if (!state) {
                                mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                            }
                        } else if (grimAttackedFlightObjectValue.getValue()) {
                            for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                                if (entity instanceof IProjectile && entity != mc.thePlayer) {
                                    if (entity.onGround || mc.thePlayer.getDistanceToEntity(entity) > 6) continue;

                                    needUpdate = true;

                                    boolean state = EntityPlayerSP.serverSprintState;

                                    if (!state) {
                                        mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                                    }

                                    for (int i = 0; i < 8; i++) {
                                        mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK));
                                        mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                                    }

                                    reduceXZ = getMotion();

                                    if (!state) {
                                        mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                                    }
                                    break;
                                }
                            }
                        }

                        break;
                    }
                    case "attack reduce": {
                        if (KillAura.INSTANCE.target != null && mc.getNetHandler() != null) {
                            event.setCancelled();

                            if (!EntityPlayerSP.serverSprintState) {
                                mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                            }

                            for (int i = 0;i < 8;i++) {
                                mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(KillAura.INSTANCE.target, C02PacketUseEntity.Action.ATTACK));
                                mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                            }


                            double velocityX = wrapped.motionX / 8000.0;
                            double velocityZ = wrapped.motionZ / 8000.0;

                            final double offset = getMotion();
                            mc.thePlayer.motionX = velocityX * offset;
                            mc.thePlayer.motionZ = velocityZ * offset;

                            mc.thePlayer.motionY = wrapped.motionY / 8000.0;

                            if (!EntityPlayerSP.serverSprintState)
                                mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                        }

                        break;
                    }
                }
            }
        }
    };

    private double getMotion() {
        return 0.07776d;
    }
}
