package cn.hackedmc.urticaria.component.impl.hypixel;

import cn.hackedmc.urticaria.component.Component;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.chat.ChatUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2DPacketOpenWindow;

public class InventoryDeSyncComponent extends Component {

    private static boolean active, deSynced;

    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        Packet<?> p = event.getPacket();

        if (p instanceof S2DPacketOpenWindow) {
            if (active) {
                event.setCancelled();
                deSynced = true;
                active = false;
            }
        }
    };

    public static void setActive(String command) {
        if (active || deSynced || InstanceAccess.mc.currentScreen != null) {
            return;
        }

        ChatUtil.send(command);
        active = true;
    }

    public static boolean isDeSynced() {
        return deSynced;
    }
}
