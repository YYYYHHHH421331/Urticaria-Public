package cn.hackedmc.urticaria.module.impl.movement.noweb;

import cn.hackedmc.urticaria.module.impl.movement.NoWeb;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.LivingUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.RayCastUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class GrimACNoWeb extends Mode<NoWeb> {
    public GrimACNoWeb(String name, NoWeb parent) {
        super(name, parent);
    }
    @EventLink
    private final Listener<LivingUpdateEvent> onPreMotion = event -> {
        if (RayCastUtil.isOnBlock() && GameSettings.isKeyDown(mc.gameSettings.keyBindAttack)) return;

        for (int i = -3;i <= 3;i++) {
            for (int i2 = -4;i2 <= 4;i2++) {
                for (int i3 = -3;i3 <= 3;i3++) {
                    final BlockPos bp = new BlockPos(mc.thePlayer).add(i, i2, i3);
                    final Block block = mc.theWorld.getBlockState(bp).getBlock();
                    if (block instanceof BlockWeb)
                        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, bp, EnumFacing.UP));
                }
            }
        }

        mc.thePlayer.isInWeb = false;
    };
}
