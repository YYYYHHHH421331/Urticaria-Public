package cn.hackedmc.urticaria.module.impl.ghost;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.impl.other.TickEvent;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;

@Rise
@ModuleInfo(name = "module.ghost.autoplace.name", description = "module.ghost.autoplace.description", category = Category.GHOST)
public class AutoPlace extends Module {
    private final BooleanValue noSwing = new BooleanValue("No Swing", this, false);

    @EventLink
    public final Listener<TickEvent> onTick = event -> {
        if (mc.objectMouseOver != null) {
            if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.sideHit != EnumFacing.DOWN && mc.objectMouseOver.sideHit != EnumFacing.UP && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock) {
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec)) {
                    if (noSwing.getValue()) {
                        PacketUtil.send(new C0APacketAnimation());
                    } else {
                        mc.thePlayer.swingItem();
                    }
                }
            }
        }
    };
}
