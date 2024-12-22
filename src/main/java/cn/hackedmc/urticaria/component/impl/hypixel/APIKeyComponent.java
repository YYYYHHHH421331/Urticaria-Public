package cn.hackedmc.urticaria.component.impl.hypixel;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.component.Component;
import cn.hackedmc.urticaria.module.impl.render.SniperOverlay;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.ServerJoinEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.util.player.ServerUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Rise
public final class APIKeyComponent extends Component {

    private final Pattern pattern = Pattern.compile("Your new API key is (.*)");

    public static boolean receivedKey;
    public static String apiKey;

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (!receivedKey && mc.thePlayer.ticksExisted == 2 && ServerUtil.isOnServer("hypixel")) {
            if (Client.INSTANCE.getModuleManager().get(SniperOverlay.class).isEnabled()) mc.thePlayer.sendChatMessage("/api new");
        }
    };

    @EventLink()
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {

        final Packet<?> p = event.getPacket();
        if (p instanceof S02PacketChat) {
            if (!ServerUtil.isOnServer("hypixel")) {
                return;
            }

            final S02PacketChat wrapper = (S02PacketChat) p;
            final String message = wrapper.getChatComponent().getUnformattedText();
            final Matcher matcher = pattern.matcher(message);

            if (!wrapper.isChat() && matcher.find()) {
                apiKey = matcher.group(1);

                if (!receivedKey) {
                    event.setCancelled(true);
                }

                receivedKey = true;
            }
        }
    };

    @EventLink()
    public final Listener<ServerJoinEvent> onServerJoin = event -> {
        APIKeyComponent.receivedKey = false;
        APIKeyComponent.apiKey = null;
    };
}
