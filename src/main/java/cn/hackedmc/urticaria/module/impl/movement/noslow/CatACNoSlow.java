package cn.hackedmc.urticaria.module.impl.movement.noslow;

import cn.hackedmc.urticaria.module.impl.movement.NoSlow;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PostMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.SlowDownEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class CatACNoSlow extends Mode<NoSlow> {
    public CatACNoSlow(String name, NoSlow parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && mc.thePlayer.isUsingItem())
            PacketUtil.sendNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(-1, -1, -1), EnumFacing.DOWN));
    };

    @EventLink
    public final Listener<PostMotionEvent> onPostMotion = event -> {
        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && mc.thePlayer.isUsingItem())
            PacketUtil.sendNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
    };

    @EventLink
    public final Listener<SlowDownEvent> onSlowDown = event -> {
        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && mc.thePlayer.isUsingItem()) event.setCancelled();
    };
}
