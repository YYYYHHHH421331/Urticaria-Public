package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.LivingUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.other.MoveEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleInfo(name = "module.player.fastladder.name", description = "module.player.fastladder,description", category = Category.PLAYER)
public class FastLadder extends Module {
    @EventLink
    private final Listener<LivingUpdateEvent> onPreMotion = event -> {
        if (GameSettings.isKeyDown(mc.gameSettings.keyBindForward) || mc.thePlayer.rotationPitch <= 0) return;

        for (int i = -3;i <= 3;i++) {
            for (int i2 = -2;i2 <= 2;i2++) {
                for (int i3 = -3;i3 <= 3;i3++) {
                    final BlockPos bp = new BlockPos(mc.thePlayer).add(i, i2, i3);
                    final Block block = mc.theWorld.getBlockState(bp).getBlock();
                    if (block instanceof BlockLadder) {
                        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, bp, EnumFacing.UP));
                        mc.theWorld.setBlockToAir(bp);
                    }
                }
            }
        }
    };

    @EventLink
    private final Listener<MoveEvent> onMove = event -> {
        if (mc.thePlayer.isOnLadder()) {
            final BlockPos bp = new BlockPos(mc.thePlayer).up();
            final Block block = mc.theWorld.getBlockState(bp).getBlock();
            if (mc.thePlayer.isCollidedHorizontally && block instanceof BlockLadder) {
                event.setPosY(0.1786);
            }
        }
    };
}
