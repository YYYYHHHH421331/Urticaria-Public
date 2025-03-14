package cn.hackedmc.urticaria.module.impl.player.fastuse;

import cn.hackedmc.urticaria.module.impl.player.FastUse;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class NCPFastUse extends Mode<FastUse> {
    public NCPFastUse(String name, FastUse parent) {
        super(name, parent);
    }

    @EventLink
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem() != null) {
            if (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) {
                if (mc.thePlayer.getItemInUseCount() == 71991 && getParent().bow.getValue()) {
                    for (int i = 0;i < 11;i++) mc.getNetHandler().addToSendQueue(new C03PacketPlayer(mc.thePlayer.onGround));
                    mc.thePlayer.itemInUseCount = 71980;
                    if (getParent().autoShot.getValue()) {
                        mc.thePlayer.stopUsingItem();
                        if (!mc.isSingleplayer()) mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    }
                }
            } else if (!(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) {
                if (mc.thePlayer.getItemInUseCount() == 22 && getParent().food.getValue()) {
                    for (int i = 0;i < 22;i++) mc.getNetHandler().addToSendQueue(new C03PacketPlayer(mc.thePlayer.onGround));
                    mc.thePlayer.itemInUseCount = 0;
                    mc.thePlayer.stopUsingItem();
                }
            }
        }
    };
}
