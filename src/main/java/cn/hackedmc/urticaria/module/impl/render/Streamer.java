package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.StringValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

@ModuleInfo(name = "module.render.streamer.name", description = "module.render.streamer.description", category = Category.RENDER)
public final class Streamer extends Module  {

    public final BooleanValue name = new BooleanValue("Name", this, true);
    public final StringValue replacement = new StringValue("Replacement", this, "You");

    @EventLink()
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {

        final Packet<?> packet = event.getPacket();

        if (packet instanceof S02PacketChat && name.getValue()) {
            final S02PacketChat wrapper = ((S02PacketChat) packet);
            final IChatComponent iChatComponent = wrapper.getChatComponent();

            if (iChatComponent instanceof ChatComponentText) {
                final String newMessage = iChatComponent.getFormattedText().replace(
                        mc.thePlayer.getGameProfile().getName(), this.replacement.getValue());

                final ChatComponentText newChatComponentText = new ChatComponentText(newMessage);

                wrapper.setChatComponent(newChatComponentText);
            }

            event.setPacket(wrapper);
        }
    };
}