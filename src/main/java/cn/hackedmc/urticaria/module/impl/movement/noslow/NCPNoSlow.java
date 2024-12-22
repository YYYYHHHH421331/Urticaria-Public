package cn.hackedmc.urticaria.module.impl.movement.noslow;

import cn.hackedmc.urticaria.module.impl.movement.NoSlow;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.SlowDownEvent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

/**
 * @author Auth
 * @since 18/11/2021
 */

public class NCPNoSlow extends Mode<NoSlow> {
    public NCPNoSlow(String name, NoSlow parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (InstanceAccess.mc.thePlayer.isBlocking()) {
            PacketUtil.sendNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), EnumFacing.UP.getIndex(), null, 0.0F, 0.0F, 0.0F));
        }
    };

    @EventLink
    public final Listener<SlowDownEvent> onSlowDown = event -> {
        event.setCancelled(true);
    };
}