package cn.hackedmc.urticaria.module.impl.player.antivoid;

import cn.hackedmc.urticaria.component.impl.player.FallDistanceComponent;
import cn.hackedmc.urticaria.module.impl.player.AntiVoid;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.MoveEvent;
import cn.hackedmc.urticaria.newevent.impl.other.MoveMathEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S18PacketEntityTeleport;

public class FreezeAntiVoid extends Mode<AntiVoid> {
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Less Move"))
            .add(new SubMode("Minecraft Move"))
            .setDefault("Less Move");
    private final NumberValue distance = new NumberValue("Distance", this, 5, 0, 10, 1);

    public FreezeAntiVoid(String name, AntiVoid parent) {
        super(name, parent);
    }
    public static boolean running;
    private Packet<?> packetWaitForSend;
    private int skipTick;

    @Override
    public void onEnable() {
        packetWaitForSend = null;
        running = false;
        skipTick = 0;
    }

    @Override
    public void onDisable() {
        running = false;
        skipTick = 0;
    }

    @EventLink
    private final Listener<MoveMathEvent> onMoveMath = event -> {
        if (mode.getValue().getName().equalsIgnoreCase("Minecraft Move")) {
            if (mc.thePlayer.positionUpdateTicks < 20 && !mc.thePlayer.onGround && !PlayerUtil.isBlockUnder(50, true) && mc.playerController.getCurrentGameType().isSurvivalOrAdventure() && FallDistanceComponent.distance > distance.getValue().floatValue() && skipTick == 0) event.setCancelled();
            else if (skipTick > 0) skipTick--;
        } else if (running)
            event.setCancelled();
    };

    @EventLink
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (!mode.getValue().getName().equalsIgnoreCase("Less Move")) return;

        if (mc.thePlayer.positionUpdateTicks < 20 && !mc.thePlayer.onGround && !PlayerUtil.isBlockUnder(50, true) && mc.playerController.getCurrentGameType().isSurvivalOrAdventure() && FallDistanceComponent.distance > distance.getValue().floatValue() && skipTick == 0) {
            if (!running) {
                running = true;
            }

            event.setCancelled();
        } else {
            if (skipTick > 0) skipTick--;
            if (running) {
                running = false;
            }
        }
    };

    @EventLink
    private final Listener<PacketSendEvent> onPacketSend = event -> {
        if (!mode.getValue().getName().equalsIgnoreCase("Less Move")) return;

        final Packet<?> packet = event.getPacket();

        if (running && packet instanceof C08PacketPlayerBlockPlacement) {
            final C08PacketPlayerBlockPlacement wrapped = (C08PacketPlayerBlockPlacement) packet;

            if (wrapped.getStack() != null) {
                if (wrapped.getStack().getItem() instanceof ItemEnderPearl || wrapped.getStack().getItem() instanceof ItemEgg || wrapped.getStack().getItem() instanceof ItemSnowball || wrapped.getStack().getItem() instanceof ItemBlock) {
                    event.setCancelled();
                    updateRotation();
                    packetWaitForSend =  wrapped;
                }
            }
        }

        if (running && packet instanceof C07PacketPlayerDigging) {
            final C07PacketPlayerDigging wrapped = (C07PacketPlayerDigging) packet;

            if (wrapped.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) {
                    event.setCancelled();
                    updateRotation();
                    packetWaitForSend = packet;
                }
            }
        }
        if (packetWaitForSend != null && packet instanceof C0FPacketConfirmTransaction) {
            event.setCancelled();
            mc.getNetHandler().addToSendQueueUnregistered(packet);
            mc.getNetHandler().addToSendQueueUnregistered(packetWaitForSend);
            packetWaitForSend = null;
        }
    };

    @EventLink
    private final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof S08PacketPlayerPosLook) {
            running = false;
        }

        if (packet instanceof S08PacketPlayerPosLook) {
            skipTick = 2;
        }

        if (packet instanceof S18PacketEntityTeleport) {
            final S18PacketEntityTeleport wrapped = (S18PacketEntityTeleport) packet;

            if (wrapped.entityId == mc.thePlayer.getEntityId())
                skipTick = 2;
        }
    };

    public void updateRotation() {
        if (mc.thePlayer.rotationYaw != mc.thePlayer.lastReportedYaw || mc.thePlayer.rotationPitch != mc.thePlayer.lastReportedPitch) {
            mc.getNetHandler().addToSendQueueUnregistered(new C03PacketPlayer.C05PacketPlayerLook(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
            mc.thePlayer.lastReportedYaw = mc.thePlayer.rotationYaw;
            mc.thePlayer.lastReportedPitch = mc.thePlayer.rotationPitch;
            mc.thePlayer.positionUpdateTicks++;
        }
    }
}
