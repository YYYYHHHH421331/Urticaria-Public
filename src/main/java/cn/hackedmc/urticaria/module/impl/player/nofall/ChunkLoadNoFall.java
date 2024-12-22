package cn.hackedmc.urticaria.module.impl.player.nofall;

import cn.hackedmc.urticaria.component.impl.player.FallDistanceComponent;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.TeleportEvent;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.module.impl.player.NoFall;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;

/**
 * @author Strikeless
 * @since 12.05.2022
 */
public class ChunkLoadNoFall extends Mode<NoFall> {

    private boolean fakeUnloaded;

    public ChunkLoadNoFall(String name, NoFall parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (fakeUnloaded) {
            mc.thePlayer.motionY = 0.0D;

            event.setOnGround(false);
            event.setPosY(event.getPosY() - 0.098F);
            mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, event.getPosY(), mc.thePlayer.posZ);

            return;
        }

        if (mc.thePlayer.motionY > 0.0D || FallDistanceComponent.distance <= 3.0F) {
            return;
        }

        final Block nextBlock = PlayerUtil.block(new BlockPos(
                event.getPosX(),
                event.getPosY() + mc.thePlayer.motionY,
                event.getPosZ()
        ));

        if (nextBlock.getMaterial().isSolid()) {
            FallDistanceComponent.distance = 0.0F;
            fakeUnloaded = true;
        }
    };

    @EventLink()
    public final Listener<TeleportEvent> onTeleport = event -> {
        fakeUnloaded = false;
    };
}
