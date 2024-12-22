package cn.hackedmc.urticaria.module.impl.other.protocols;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.module.impl.other.ServerProtocol;
import cn.hackedmc.urticaria.module.impl.other.protocols.hyt.forge.ForgeChannel;
import cn.hackedmc.urticaria.module.impl.other.protocols.hyt.germ.GermWrapper;
import cn.hackedmc.urticaria.module.impl.other.protocols.hyt.vexview.VexViewWrapper;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.other.WorldChangeEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.util.chat.ChatUtil;
import cn.hackedmc.urticaria.value.Mode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.tools.javac.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class HuaYuTingProtocol extends Mode<ServerProtocol> {
    public static final byte[] MOD_LIST = new byte[]{2, 28, 9, 109, 105, 110, 101, 99, 114, 97, 102, 116, 6, 49, 46, 49, 50, 46, 50, 9, 100, 101, 112, 97, 114, 116, 109, 111, 100, 3, 49, 46, 48, 13, 115, 99, 114, 101, 101, 110, 115, 104, 111, 116, 109, 111, 100, 3, 49, 46, 48, 3, 101, 115, 115, 5, 49, 46, 48, 46, 50, 7, 118, 101, 120, 118, 105, 101, 119, 6, 50, 46, 54, 46, 49, 48, 18, 98, 97, 115, 101, 109, 111, 100, 110, 101, 116, 101, 97, 115, 101, 99, 111, 114, 101, 5, 49, 46, 57, 46, 52, 10, 115, 105, 100, 101, 98, 97, 114, 109, 111, 100, 3, 49, 46, 48, 11, 115, 107, 105, 110, 99, 111, 114, 101, 109, 111, 100, 6, 49, 46, 49, 50, 46, 50, 15, 102, 117, 108, 108, 115, 99, 114, 101, 101, 110, 112, 111, 112, 117, 112, 12, 49, 46, 49, 50, 46, 50, 46, 51, 56, 48, 48, 48, 8, 115, 116, 111, 114, 101, 109, 111, 100, 3, 49, 46, 48, 3, 109, 99, 112, 4, 57, 46, 52, 50, 7, 115, 107, 105, 110, 109, 111, 100, 3, 49, 46, 48, 13, 112, 108, 97, 121, 101, 114, 109, 97, 110, 97, 103, 101, 114, 3, 49, 46, 48, 13, 100, 101, 112, 97, 114, 116, 99, 111, 114, 101, 109, 111, 100, 6, 49, 46, 49, 50, 46, 50, 9, 109, 99, 98, 97, 115, 101, 109, 111, 100, 3, 49, 46, 48, 17, 109, 101, 114, 99, 117, 114, 105, 117, 115, 95, 117, 112, 100, 97, 116, 101, 114, 3, 49, 46, 48, 3, 70, 77, 76, 9, 56, 46, 48, 46, 57, 57, 46, 57, 57, 11, 110, 101, 116, 101, 97, 115, 101, 99, 111, 114, 101, 6, 49, 46, 49, 50, 46, 50, 7, 97, 110, 116, 105, 109, 111, 100, 3, 50, 46, 48, 11, 102, 111, 97, 109, 102, 105, 120, 99, 111, 114, 101, 5, 55, 46, 55, 46, 52, 10, 110, 101, 116, 119, 111, 114, 107, 109, 111, 100, 6, 49, 46, 49, 49, 46, 50, 7, 102, 111, 97, 109, 102, 105, 120, 9, 64, 86, 69, 82, 83, 73, 79, 78, 64, 5, 102, 111, 114, 103, 101, 12, 49, 52, 46, 50, 51, 46, 53, 46, 50, 55, 54, 56, 13, 102, 114, 105, 101, 110, 100, 112, 108, 97, 121, 109, 111, 100, 3, 49, 46, 48, 4, 108, 105, 98, 115, 5, 49, 46, 48, 46, 50, 9, 102, 105, 108, 116, 101, 114, 109, 111, 100, 3, 49, 46, 48, 7, 103, 101, 114, 109, 109, 111, 100, 5, 51, 46, 52, 46, 50, 9, 112, 114, 111, 109, 111, 116, 105, 111, 110, 14, 49, 46, 48, 46, 48, 45, 83, 78, 65, 80, 83, 72, 79, 84};
    public static final byte[] REGISTER_CHANNEL = new byte[]{70, 77, 76, 124, 72, 83, 0, 70, 77, 76, 0, 70, 77, 76, 124, 77, 80, 0, 70, 77, 76, 0, 97, 110, 116, 105, 109, 111, 100, 0, 67, 104, 97, 116, 86, 101, 120, 86, 105, 101, 119, 0, 66, 97, 115, 101, 54, 52, 86, 101, 120, 86, 105, 101, 119, 0, 72, 117, 100, 66, 97, 115, 101, 54, 52, 86, 101, 120, 86, 105, 101, 119, 0, 70, 79, 82, 71, 69, 0, 103, 101, 114, 109, 112, 108, 117, 103, 105, 110, 45, 110, 101, 116, 101, 97, 115, 101, 0, 86, 101, 120, 86, 105, 101, 119, 0, 104, 121, 116, 48, 0, 97, 114, 109, 111, 117, 114, 101, 114, 115, 0, 112, 114, 111, 109, 111, 116, 105, 111, 110};

    private final GermWrapper germWrapper = new GermWrapper();
    private final VexViewWrapper vexViewWrapper = new VexViewWrapper();
    public ForgeChannel forgeChannel = new ForgeChannel();

    public HuaYuTingProtocol(String name, ServerProtocol parent) {
        super(name, parent);

        buttons.put("起床练习",new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 48, 34, 123, 34, 101, 110, 116, 114, 121, 34, 58, 48, 44, 34, 115, 105, 100, 34, 58, 34, 66, 69, 68, 87, 65, 82, 47, 98, 119, 45, 100, 97, 108, 117, 34, 125});
        buttons.put("起床单人",new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 49, 34, 123, 34, 101, 110, 116, 114, 121, 34, 58, 49, 44, 34, 115, 105, 100, 34, 58, 34, 66, 69, 68, 87, 65, 82, 47, 98, 119, 45, 115, 111, 108, 111, 34, 125});
        buttons.put("起床双人",new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 50, 36, 123, 34, 101, 110, 116, 114, 121, 34, 58, 50, 44, 34, 115, 105, 100, 34, 58, 34, 66, 69, 68, 87, 65, 82, 47, 98, 119, 45, 100, 111, 117, 98, 108, 101, 34, 125});
        buttons.put("起床四人",new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 51, 34, 123, 34, 101, 110, 116, 114, 121, 34, 58, 51, 44, 34, 115, 105, 100, 34, 58, 34, 66, 69, 68, 87, 65, 82, 47, 98, 119, 45, 116, 101, 97, 109, 34, 125});
        buttons.put("空岛单人",new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 48, 34, 123, 34, 101, 110, 116, 114, 121, 34, 58, 48, 44, 34, 115, 105, 100, 34, 58, 34, 83, 75, 89, 87, 65, 82, 47, 110, 115, 107, 121, 119, 97, 114, 34, 125});
        buttons.put("空岛双人",new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 49, 41, 123, 34, 101, 110, 116, 114, 121, 34, 58, 49, 44, 34, 115, 105, 100, 34, 58, 34, 83, 75, 89, 87, 65, 82, 47, 110, 115, 107, 121, 119, 97, 114, 45, 100, 111, 117, 98, 108, 101, 34, 125});
        buttons.put("职业战争",new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 50, 33, 123, 34, 101, 110, 116, 114, 121, 34, 58, 50, 44, 34, 115, 105, 100, 34, 58, 34, 70, 73, 71, 72, 84, 47, 107, 98, 45, 103, 97, 109, 101, 34, 125});
        buttons.put("天坑",new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 52, 33, 123, 34, 101, 110, 116, 114, 121, 34, 58, 52, 44, 34, 115, 105, 100, 34, 58, 34, 70, 73, 71, 72, 84, 47, 116, 104, 101, 45, 112, 105, 116, 34, 125});
    }
    private final HashMap<String, byte[]> buttons = new HashMap<>();

    @Override
    public void onEnable() {
        germWrapper.reset();
    }

    @EventLink
    private final Listener<WorldChangeEvent> onWorld = event -> {
        germWrapper.reset();
    };

    @EventLink()
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C01PacketChatMessage) {
            final C01PacketChatMessage wrapped = (C01PacketChatMessage) packet;

            this.germWrapper.processCommand(wrapped.getMessage());
        }
    };

    public void processPacket(S3FPacketCustomPayload packet) {
        if (packet.getChannelName().equalsIgnoreCase("FML|HS") || packet.getChannelName().equalsIgnoreCase("REGISTER") || packet.getChannelName().equalsIgnoreCase("UNREGISTER")) {
            this.forgeChannel.processForge(packet);
        } else if (packet.getChannelName().equalsIgnoreCase("germplugin-netease")) {
            this.germWrapper.processGerm(packet);
        } else if (packet.getChannelName().equals("VexView")) {
            this.vexViewWrapper.processVewView(packet);
        }
    }
}
