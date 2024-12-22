package cn.hackedmc.urticaria.module.impl.other.protocols.hyt;

import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

public class PacketChecker implements InstanceAccess {
    public static void sendToServer(String channelName, PacketBuffer buffer) {
        if (mc.getNetHandler() == null) return;
        mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload(channelName, buffer));
    }
}
