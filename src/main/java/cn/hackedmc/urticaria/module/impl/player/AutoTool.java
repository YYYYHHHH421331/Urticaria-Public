package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.player.SlotComponent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.combat.KillAura;
import cn.hackedmc.urticaria.module.impl.other.Nuker;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.other.BlockBreakEvent;
import cn.hackedmc.urticaria.newevent.impl.other.BlockDamageEvent;
import cn.hackedmc.urticaria.newevent.impl.other.ClientTickEvent;
import cn.hackedmc.urticaria.newevent.impl.other.TickEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.player.SlotUtil;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

/**
 * @author Alan (made good code)
 * @since 24/06/2023
 */

@Rise
@ModuleInfo(name = "module.player.autotool.name", description = "module.player.autotool.description", category = Category.PLAYER)
public class AutoTool extends Module {
    public ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Basic"))
            .add(new SubMode("Spoof"))
            .add(new SubMode("Silent"))
            .setDefault("Basic");
    private int slot;
    private int lastSlot;

    @Override
    protected void onEnable() {
        slot = -1;
    }

    @EventLink
    public final Listener<BlockBreakEvent> onBlockBreak = event -> {
        if (slot != -1) {
            if (mode.getValue().getName().equalsIgnoreCase("Basic")) {
                mc.thePlayer.inventory.currentItem = slot;
            } else {
                SlotComponent.setSlot(slot, true);
            }
            switch (mode.getValue().getName().toLowerCase()) {
                case "basic": {
                    mc.thePlayer.inventory.currentItem = slot;

                    break;
                }
                case "spoof": {
                    SlotComponent.setSlot(slot, true);

                    break;
                }
                case "silent": {
                    SlotComponent.setSlotNative(slot, true);

                    break;
                }
            }
        }
    };

    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C07PacketPlayerDigging && lastSlot != -1) {
            final C07PacketPlayerDigging wrapped = (C07PacketPlayerDigging) packet;

            if (wrapped.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK || wrapped.getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK || wrapped.getStatus() == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
                event.setCancelled();
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(lastSlot));
                mc.getNetHandler().addToSendQueueUnregistered(wrapped);
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            }
        }
    };

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (mc.thePlayer == null || mc.theWorld == null || getModule(Scaffold.class).isEnabled()) return;

        boolean isSword = false;
        if (mc.objectMouseOver != null) {
            switch (InstanceAccess.mc.objectMouseOver.typeOfHit) {
                case BLOCK:
                    if (KillAura.INSTANCE.target == null || Nuker.INSTANCE.isEnabled())
                        lastSlot = slot = SlotUtil.findTool(mc.objectMouseOver.getBlockPos());
                    else
                        lastSlot = slot = -1;
                    break;

                case ENTITY:
                    slot = SlotUtil.findSword();
                    isSword = true;
                    break;
            }
        }

        if (slot == mc.thePlayer.inventory.currentItem) lastSlot = slot = -1;

        switch (mode.getValue().getName().toLowerCase()) {
            case "spoof": {
                if (lastSlot != -1 && mc.gameSettings.keyBindAttack.isKeyDown()) {
                    SlotComponent.setSlot(lastSlot, true);
                } else {
                    lastSlot = -1;
                }

                break;
            }

            case "silent": {
                if (lastSlot != -1 && mc.gameSettings.keyBindAttack.isKeyDown()) {
                    SlotComponent.setSlotNative(lastSlot, true);
                } else {
                    lastSlot = -1;
                }

                break;
            }
        }

        if (KillAura.INSTANCE.target != null && slot != -1 && isSword) {
            switch (mode.getValue().getName().toLowerCase()) {
                case "basic": {
                    mc.thePlayer.inventory.currentItem = slot;

                    break;
                }
                case "spoof": {
                    SlotComponent.setSlot(slot, true);

                    break;
                }
                case "silent": {
                    if (!Nuker.INSTANCE.isEnabled()) {
                        SlotComponent.setSlot(slot, true);
                    }

                    break;
                }
            }
        }
    };
}