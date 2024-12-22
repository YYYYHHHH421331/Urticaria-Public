package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@Rise
@ModuleInfo(name = "module.player.antisuffocate.name", description = "module.player.antifsuffocate.description", category = Category.PLAYER)
public class AntiSuffocate extends Module {

    private final BooleanValue swing = new BooleanValue("Swing", this, true);


    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (InstanceAccess.mc.thePlayer.isEntityInsideOpaqueBlock()) {
            PacketUtil.send(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, new BlockPos(InstanceAccess.mc.thePlayer).down(), EnumFacing.UP));

            if (swing.getValue()) {
                InstanceAccess.mc.thePlayer.swingItem();
            }
        }
    };
}