package cn.hackedmc.urticaria.module.impl.movement.noslow;

import cn.hackedmc.urticaria.module.impl.combat.KillAura;
import cn.hackedmc.urticaria.module.impl.movement.NoSlow;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PostMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.SlowDownEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class WatchdogNoSlow extends Mode<NoSlow> {
    public WatchdogNoSlow(String name, NoSlow parent) {
        super(name, parent);
    }

    private boolean send;

    @Override
    public void onEnable() {
        send = false;
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem() != null) {
            if (mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
            }
        }
    };

    @EventLink
    public final Listener<SlowDownEvent> onSlowDown = event -> {
        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)
            event.setCancelled(true);
    };
}
