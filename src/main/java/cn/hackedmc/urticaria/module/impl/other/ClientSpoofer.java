package cn.hackedmc.urticaria.module.impl.other;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import io.netty.buffer.Unpooled;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

@Rise
@ModuleInfo(name = "module.other.clientspoofer.name", description = "module.other.clientspoofer.description", category = Category.OTHER)
public final class ClientSpoofer extends Module  {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Forge"))
            .add(new SubMode("Lunar"))
            .add(new SubMode("PvP Lounge"))
            .add(new SubMode("CheatBreaker"))
            .add(new SubMode("Geyser"))
            .add(new SubMode("Germ Mod"))
            .setDefault("Forge");

    @EventLink()
    public final Listener<PacketSendEvent> onPacketSend = event -> {

        final Packet<?> packet = event.getPacket();

        if (packet instanceof C17PacketCustomPayload) {
            final C17PacketCustomPayload wrapper = (C17PacketCustomPayload) packet;

            if (!wrapper.getChannelName().equalsIgnoreCase("MC|Brand")) return;

            switch (mode.getValue().getName()) {
                case "Forge": {
                    wrapper.setData(createPacketBuffer("fml,forge", true));
                    break;
                }

                case "Lunar": {
                    wrapper.setChannel("REGISTER");
                    wrapper.setData(createPacketBuffer("Lunar-Client", false));
                    break;
                }

                case "LabyMod": {
                    wrapper.setData(createPacketBuffer("LMC", true));
                    break;
                }

                case "PvP Lounge": {
                    wrapper.setData(createPacketBuffer("PLC18", false));
                    break;
                }

                case "CheatBreaker": {
                    wrapper.setData(createPacketBuffer("CB", true));
                    break;
                }

                case "Geyser": {
                    // It's meant to be "eyser" don't change it
                    wrapper.setData(createPacketBuffer("eyser", false));
                    break;
                }

                case "Germ Mod": {
                    wrapper.setData(createPacketBuffer("fml,forge", true));
//                    final byte[] payload = new byte[]{2, 32, 9, 109, 105, 110, 101, 99, 114, 97, 102, 116, 6, 49, 46, 49, 50, 46, 50, 9, 100, 101, 112, 97, 114, 116, 109, 111, 100, 3, 49, 46, 48, 19, 97, 114, 109, 111, 117, 114, 101, 114, 115, 95, 119, 111, 114, 107, 115, 104, 111, 112, 115, 13, 49, 46, 49, 50, 46, 50, 45, 48, 46, 52, 57, 46, 49, 13, 115, 99, 114, 101, 101, 110, 115, 104, 111, 116, 109, 111, 100, 3, 49, 46, 48, 18, 98, 97, 115, 101, 109, 111, 100, 110, 101, 116, 101, 97, 115, 101, 99, 111, 114, 101, 5, 49, 46, 57, 46, 52, 11, 115, 107, 105, 110, 99, 111, 114, 101, 109, 111, 100, 6, 49, 46, 49, 50, 46, 50, 8, 110, 115, 104, 111, 119, 109, 111, 100, 3, 49, 46, 48, 10, 115, 109, 111, 111, 116, 104, 102, 111, 110, 116, 12, 109, 99, 49, 46, 49, 50, 46, 50, 45, 50, 46, 48, 15, 102, 117, 108, 108, 115, 99, 114, 101, 101, 110, 112, 111, 112, 117, 112, 12, 49, 46, 49, 50, 46, 50, 46, 51, 56, 48, 48, 48, 12, 120, 97, 101, 114, 111, 109, 105, 110, 105, 109, 97, 112, 6, 50, 49, 46, 52, 46, 49, 3, 109, 99, 112, 4, 57, 46, 52, 50, 7, 115, 107, 105, 110, 109, 111, 100, 3, 49, 46, 48, 14, 115, 109, 111, 111, 116, 104, 102, 111, 110, 116, 99, 111, 114, 101, 12, 109, 99, 49, 46, 49, 50, 46, 50, 45, 50, 46, 48, 13, 112, 108, 97, 121, 101, 114, 109, 97, 110, 97, 103, 101, 114, 3, 49, 46, 48, 13, 100, 101, 112, 97, 114, 116, 99, 111, 114, 101, 109, 111, 100, 6, 49, 46, 49, 50, 46, 50, 9, 109, 99, 98, 97, 115, 101, 109, 111, 100, 3, 49, 46, 48, 17, 109, 101, 114, 99, 117, 114, 105, 117, 115, 95, 117, 112, 100, 97, 116, 101, 114, 3, 49, 46, 48, 16, 115, 104, 117, 108, 107, 101, 114, 98, 111, 120, 118, 105, 101, 119, 101, 114, 3, 49, 46, 53, 3, 70, 77, 76, 9, 56, 46, 48, 46, 57, 57, 46, 57, 57, 11, 110, 101, 116, 101, 97, 115, 101, 99, 111, 114, 101, 6, 49, 46, 49, 50, 46, 50, 7, 97, 110, 116, 105, 109, 111, 100, 3, 50, 46, 48, 11, 102, 111, 97, 109, 102, 105, 120, 99, 111, 114, 101, 5, 55, 46, 55, 46, 52, 10, 110, 101, 116, 119, 111, 114, 107, 109, 111, 100, 6, 49, 46, 49, 49, 46, 50, 17, 120, 97, 101, 114, 111, 109, 105, 110, 105, 109, 97, 112, 95, 99, 111, 114, 101, 10, 49, 46, 49, 50, 46, 50, 45, 49, 46, 48, 7, 102, 111, 97, 109, 102, 105, 120, 14, 48, 46, 49, 48, 46, 49, 48, 45, 49, 46, 49, 50, 46, 50, 11, 110, 101, 116, 111, 112, 116, 105, 109, 105, 122, 101, 3, 49, 46, 48, 5, 102, 111, 114, 103, 101, 12, 49, 52, 46, 50, 51, 46, 53, 46, 50, 55, 54, 56, 10, 100, 114, 97, 103, 111, 110, 99, 111, 114, 101, 5, 50, 46, 48, 46, 48, 13, 102, 114, 105, 101, 110, 100, 112, 108, 97, 121, 109, 111, 100, 3, 49, 46, 48, 21, 114, 101, 115, 111, 117, 114, 99, 101, 112, 97, 99, 107, 111, 114, 103, 97, 110, 105, 122, 101, 114, 5, 49, 46, 48, 46, 52, 9, 102, 105, 108, 116, 101, 114, 109, 111, 100, 3, 49, 46, 48, 7, 109, 111, 98, 101, 110, 100, 115, 4, 48, 46, 50, 52};
//                    mc.getNetHandler().addToSendQueueUnregistered(new C17PacketCustomPayload("FML|HS", new PacketBuffer(Unpooled.wrappedBuffer(payload))));
//                    mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("REGISTER", new PacketBuffer(Unpooled.wrappedBuffer(new byte[]{70, 77, 76, 124, 72, 83, 0, 70, 77, 76, 0, 70, 77, 76, 124, 77, 80, 0, 70, 77, 76, 0, 97, 110, 116, 105, 109, 111, 100, 0, 100, 114, 97, 103, 111, 110, 99, 111, 114, 101, 58, 109, 97, 105, 110, 0, 110, 101, 116, 111, 112, 116, 105, 109, 105, 122, 101, 0, 70, 79, 82, 71, 69, 0, 120, 97, 101, 114, 111, 109, 105, 110, 105, 109, 97, 112, 58, 109, 97, 105, 110, 0, 109, 111, 98, 101, 110, 100, 115, 0, 97, 114, 109, 111, 117, 114, 101, 114, 115}))));

                    break;
                }
            }
        }
    };

    private PacketBuffer createPacketBuffer(final String data, final boolean string) {
        if (string) {
            return new PacketBuffer(Unpooled.buffer()).writeString(data);
        } else {
            return new PacketBuffer(Unpooled.wrappedBuffer(data.getBytes()));
        }
    }
}
