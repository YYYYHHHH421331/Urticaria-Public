package cn.hackedmc.urticaria.module.impl.movement.sneak;

import cn.hackedmc.urticaria.module.impl.movement.Sneak;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PostMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.network.play.client.C0BPacketEntityAction;

/**
 * @author Auth
 * @since 25/06/2022
 */

public class NCPSneak extends Mode<Sneak> {

    public NCPSneak(String name, Sneak parent) {
        super(name, parent);
    }


    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        mc.thePlayer.movementInput.sneak = mc.thePlayer.sendQueue.doneLoadingTerrain;

        PacketUtil.send(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
    };

    @EventLink
    public final Listener<PostMotionEvent> onPostMotion = event -> {
        PacketUtil.send(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
    };

    @Override
    public void onDisable() {
        PacketUtil.send(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
    }
}