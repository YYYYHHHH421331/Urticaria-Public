package cn.hackedmc.urticaria.module.impl.movement.inventorymove;

import cn.hackedmc.urticaria.component.impl.player.BlinkComponent;
import cn.hackedmc.urticaria.module.impl.movement.InventoryMove;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.other.WorldChangeEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author LvZiQiao
 * @since 25.1.2024
 */

public class WatchdogInventoryMove extends Mode<InventoryMove> {

    public WatchdogInventoryMove(String name, InventoryMove parent) {
        super(name, parent);
    }

    private final KeyBinding[] AFFECTED_BINDINGS = new KeyBinding[]{
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindJump
    };

    private boolean openInv = false;
    private boolean cancelSprint = false;

    @Override
    public void onEnable() {
        openInv = false;
        cancelSprint = false;
    }

    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C16PacketClientStatus) {
            final C16PacketClientStatus status = (C16PacketClientStatus) packet;

            if (status.getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
                openInv = true;
                if (!cancelSprint) {
                    PacketUtil.sendNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    cancelSprint = true;
                }
            }
        }

        if (packet instanceof C0DPacketCloseWindow) {
            final C0DPacketCloseWindow window = (C0DPacketCloseWindow) packet;

            if (window.windowId == 0) {
                openInv = false;
                if (cancelSprint && EntityPlayerSP.serverSprintState) {
                    event.setCancelled();
                    PacketUtil.sendNoEvent(packet);
                    PacketUtil.sendNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    cancelSprint = false;
                }
            }
        }

        if (packet instanceof C0EPacketClickWindow) {
            final C0EPacketClickWindow window = (C0EPacketClickWindow) packet;

            if (window.windowId == 0) {
                openInv = true;
                if (!cancelSprint) {
                    PacketUtil.sendNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    cancelSprint = true;
                }
            }
        }

        if (packet instanceof C0BPacketEntityAction) {
            final C0BPacketEntityAction action = (C0BPacketEntityAction) packet;

            if (action.getAction() == C0BPacketEntityAction.Action.START_SPRINTING || action.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                if (cancelSprint)
                    event.setCancelled();
            }
        }
    };

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (mc.currentScreen instanceof GuiChat || mc.currentScreen == this.getStandardClickGUI() || mc.currentScreen instanceof GuiChest) {
            return;
        }

        for (final KeyBinding bind : AFFECTED_BINDINGS) {
            bind.setPressed(GameSettings.isKeyDown(bind));
        }

        if (!openInv) return;

        if (mc.thePlayer.ticksExisted % 3 == 0) {
            mc.getNetHandler().addToSendQueue(new C0DPacketCloseWindow(0));
        } else if (mc.thePlayer.ticksExisted % 3 == 1) {
            mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
        }
    };
}