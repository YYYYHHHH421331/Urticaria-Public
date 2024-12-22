package cn.hackedmc.urticaria.module.impl.other;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.render.notificationcomponent.NotificationComponent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.util.chat.ChatUtil;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.IChatComponent;

@Rise
@ModuleInfo(name = "module.other.autoplay.name", description = "module.other.autoplay.description", category = Category.OTHER)
public final class AutoPlay extends Module {
    private final ModeValue mode = new ModeValue("Server", this)
            .add(new SubMode("Hypixel"))
            .setDefault("Hypixel");
    private final NumberValue delay = new NumberValue("Delay", this, 3000, 0, 10000, 100);

    @EventLink()
    public final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        Packet<?> packet = event.getPacket();

        if (packet instanceof S02PacketChat) {
            S02PacketChat chat = ((S02PacketChat) packet);

            switch (mode.getValue().getName().toLowerCase()) {
                case "hypixel": {
                    if (chat.getChatComponent().getFormattedText().contains("play again?") || chat.getChatComponent().getFormattedText().contains("再来一局")) {
                        new Thread(() -> {
                            final int delayTime = delay.getValue().intValue();

                            if (delayTime != 0) {
                                try {
                                    Thread.sleep(delayTime);
                                } catch (Throwable ignored) {}
                            }

                            for (IChatComponent iChatComponent : chat.getChatComponent().getSiblings()) {
                                for (String value : iChatComponent.toString().split("'")) {
                                    if (value.startsWith("/play") && !value.contains(".")) {
                                        ChatUtil.send(value);
                                        NotificationComponent.post("Auto Play", "Joined a new game", 7000);
                                        break;
                                    }
                                }
                            }
                        }).start();
                    }

                    break;
                }
            }
        }
    };

}
