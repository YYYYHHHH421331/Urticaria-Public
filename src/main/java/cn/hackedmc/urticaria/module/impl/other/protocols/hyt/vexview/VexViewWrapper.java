package cn.hackedmc.urticaria.module.impl.other.protocols.hyt.vexview;

import cn.hackedmc.urticaria.util.chat.ChatUtil;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import com.sun.tools.javac.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.util.EnumChatFormatting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class VexViewWrapper implements InstanceAccess {
    private void sendDebugPacket(List<String> params) {
        try {
            if (params.size() < 3) {
                return;
            }
            ByteBuf data = Unpooled.wrappedBuffer(encode("{\"packet_sub_type\":\"" + params.get(0) + "\",\"packet_data\":\"" + params.get(1) + "\",\"packet_type\":\"" + params.get(2) + "\"}"));
            mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("VexView", new PacketBuffer(data)));
        } catch (Throwable ignored) {}
    }

    private String decode(byte[] byteArray) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(byteArray));
        byte[] buffer = new byte[256];
        int read;
        while ((read = gzipInputStream.read(buffer)) >= 0) {
            byteArrayOutputStream.write(buffer, 0, read);
        }
        return byteArrayOutputStream.toString("UTF-8");
    }

    private byte[] encode(String json) throws IOException {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream out = new GZIPOutputStream(bout);
        byte[] buffer = new byte[256];
        int read;
        while ((read = arrayInputStream.read(buffer)) >= 0) {
            out.write(buffer, 0, read);
        }
        out.close();
        out.finish();
        return bout.toByteArray();
    }

    public void processVewView(S3FPacketCustomPayload payload) {
        ByteBuf buffer = payload.getBufferData();
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);

        String packetString;
        try {
            packetString = decode(bytes);
        } catch (Exception e) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                stream.write(bytes);
                packetString = stream.toString("UTF-8");
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return;
            }
        }

        VexViewPacketReader packetReader = new VexViewPacketReader(packetString);
        if (packetReader.getPacketType().equals("ver")) {
            if ("get".equals(packetReader.getPacketSubType())) {
                sendDebugPacket(List.of("post", "2.6.10", "ver"));
            }
        } else if (!packetReader.getPacketType().equalsIgnoreCase("gui")) {
            ChatUtil.display("[" + EnumChatFormatting.AQUA + "VexView" + EnumChatFormatting.RESET + "] " + packetReader.getPacketType() + ": " + packetReader.getPacketData());
        }
    }
}
