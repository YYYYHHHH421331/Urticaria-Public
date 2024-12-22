package cn.hackedmc.urticaria.module.impl.player.nofall;

import cn.hackedmc.urticaria.component.impl.player.FallDistanceComponent;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.module.impl.player.NoFall;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * @author Auth
 * @since 3/02/2022
 */
public class PacketNoFall extends Mode<NoFall> {

    public PacketNoFall(String name, NoFall parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        float distance = FallDistanceComponent.distance;

        if (distance > 3) {
            PacketUtil.send(new C03PacketPlayer(true));
            distance = 0;
        }

        FallDistanceComponent.distance = distance;
    };
}