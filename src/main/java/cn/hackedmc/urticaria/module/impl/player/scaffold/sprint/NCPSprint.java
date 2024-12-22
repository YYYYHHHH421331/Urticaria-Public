package cn.hackedmc.urticaria.module.impl.player.scaffold.sprint;

import cn.hackedmc.urticaria.module.impl.player.Scaffold;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

public class NCPSprint extends Mode<Scaffold> {
    public NCPSprint(String name, Scaffold parent) {
        super(name, parent);
    }

    private int placeTime = 0;

    @Override
    public void onEnable() {
        placeTime = 0;
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        if (placeTime >= 3) {
            if (event.isOnGround()) {
//                event.setOnGround(false);
//                event.setPosY(event.getPosY() + 0.00004);
                mc.thePlayer.motionY = 0.003;
                MoveUtil.strafe(MoveUtil.speed());
            }
            placeTime = 0;
        }
    };

    @EventLink()
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        final Packet<?> p = event.getPacket();

        if (p instanceof C08PacketPlayerBlockPlacement) {
            final C08PacketPlayerBlockPlacement wrapped = (C08PacketPlayerBlockPlacement) p;

            if (wrapped.getPlacedBlockDirection() != 255)
                placeTime++;
        }
    };
}
