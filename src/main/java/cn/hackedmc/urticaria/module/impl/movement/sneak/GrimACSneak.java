package cn.hackedmc.urticaria.module.impl.movement.sneak;

import cn.hackedmc.urticaria.module.impl.exploit.Disabler;
import cn.hackedmc.urticaria.module.impl.movement.Sneak;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PostMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.network.play.client.C0BPacketEntityAction;

public class GrimACSneak extends Mode<Sneak> {
    public GrimACSneak(String name, Sneak parent) {
        super(name, parent);
    }

    private boolean check() {
        return Disabler.INSTANCE != null && Disabler.INSTANCE.isEnabled() && Disabler.INSTANCE.grimAC.getValue() && Disabler.INSTANCE.grimACDisabler.usePost();
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        mc.thePlayer.movementInput.sneak = mc.thePlayer.sendQueue.doneLoadingTerrain;

        if (check()) PacketUtil.send(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
    };

    @EventLink
    public final Listener<PostMotionEvent> onPostMotion = event -> {
        if (check()) PacketUtil.send(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
    };

    @Override
    public void onDisable() {
        PacketUtil.send(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
    }
}
