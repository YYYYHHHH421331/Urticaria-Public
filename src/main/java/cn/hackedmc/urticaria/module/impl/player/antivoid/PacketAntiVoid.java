package cn.hackedmc.urticaria.module.impl.player.antivoid;

import cn.hackedmc.urticaria.module.impl.player.AntiVoid;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import net.minecraft.network.play.client.C03PacketPlayer;

public class PacketAntiVoid extends Mode<AntiVoid> {

    private final NumberValue distance = new NumberValue("Distance", this, 5, 0, 10, 1);

    public PacketAntiVoid(String name, AntiVoid parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (mc.thePlayer.fallDistance > distance.getValue().floatValue() && !PlayerUtil.isBlockUnder()) {
            PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition());
        }
    };
}