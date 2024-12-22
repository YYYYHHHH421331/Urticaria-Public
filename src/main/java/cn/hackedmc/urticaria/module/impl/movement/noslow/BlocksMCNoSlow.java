package cn.hackedmc.urticaria.module.impl.movement.noslow;

import cn.hackedmc.urticaria.component.impl.player.SlotComponent;
import cn.hackedmc.urticaria.module.impl.movement.NoSlow;
import cn.hackedmc.urticaria.newevent.CancellableEvent;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.SlowDownEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

public class BlocksMCNoSlow extends Mode<NoSlow> {
    public BlocksMCNoSlow(String name, NoSlow parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<SlowDownEvent> onSlowDown = CancellableEvent::setCancelled;

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mc.thePlayer.getHeldItem() == null || !MoveUtil.isMoving() || !mc.thePlayer.isUsingItem()) return;

        if (mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
            PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex() % 8 + 1));
            PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex()));
        } else {
            PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        }
    };
}
