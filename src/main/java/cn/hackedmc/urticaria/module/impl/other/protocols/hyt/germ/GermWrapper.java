package cn.hackedmc.urticaria.module.impl.other.protocols.hyt.germ;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.module.impl.other.protocols.hyt.PacketChecker;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.util.chat.ChatUtil;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
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

import java.nio.charset.StandardCharsets;
import java.util.*;

public class GermWrapper implements InstanceAccess {
    private static final byte[] JOIN_GAME = new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47};
    private static final byte[] OPEN_GUI = new byte[]{0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 8, 109, 97, 105, 110, 109, 101, 110, 117, 8, 109, 97, 105, 110, 109, 101, 110, 117, 8, 109, 97, 105, 110, 109, 101, 110, 117};

    private final LinkedHashMap<String, List<GameItem>> games = new LinkedHashMap<>();
    private byte[] data;
    private int size;

    private static void sendToServer(PacketBuffer buffer) {
        PacketChecker.sendToServer("germmod-netease", buffer);
    }

    public void processGerm(S3FPacketCustomPayload payload) {
        final PacketBuffer buffer = payload.getBufferData();

        final int packetId = buffer.readInt();

        if (packetId == -1) {
            final boolean needResize = buffer.readBoolean();
            final int newSize = buffer.readInt();
            final boolean isLast = buffer.readBoolean();
            final byte[] nextArray = buffer.readByteArray();

            if (needResize) {
                data = new byte[newSize];
            }

            System.arraycopy(nextArray, 0, data, size, nextArray.length);
            size += nextArray.length;

            if (isLast) {
                ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
                final S3FPacketCustomPayload newWrapper = new S3FPacketCustomPayload("germplugin-netease", new PacketBuffer(byteBuf));

                final PacketReceiveEvent packetReceiveEvent = new PacketReceiveEvent(newWrapper);

                Client.INSTANCE.getEventBus().handle(packetReceiveEvent);

                if (!packetReceiveEvent.isCancelled())
                    newWrapper.processPacket(mc.getNetHandler());
            }
        } else if (packetId == 73) {
            final String type = buffer.readStringFromBuffer(32767);
            final String name = buffer.readStringFromBuffer(32767);
            final String data = buffer.readStringFromBuffer(99999999);

            if (type.equalsIgnoreCase("gui")) {
                if (name.equalsIgnoreCase("mainmenu")) {
                    final PacketBuffer newData = new PacketBuffer(Unpooled.buffer());

                    newData.writeInt(4);
                    newData.writeInt(0);
                    newData.writeInt(0);
                    newData.writeString("mainmenu");
                    newData.writeString("mainmenu");
                    newData.writeString("mainmenu");

                    reset();

                    sendToServer(newData);
                }
            }
        } else if (packetId == 76) {
            String string = buffer.toString(Charsets.UTF_8);
            if (!string.contains("mainmenu")) return;
            for(String n : games.keySet()){
                IChatComponent textComponents = createClickableText("[" + EnumChatFormatting.AQUA + n + EnumChatFormatting.RESET + "]", n, "/germ-btn-click list "+n);
                mc.thePlayer.addChatComponentMessage(textComponents);
            }
        } else if (packetId == 72) {
            final PacketBuffer data = new PacketBuffer(Unpooled.buffer());

            reset();

            data.writeInt(16);
            data.writeString("3.4.2");
            data.writeString(Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)));

            sendToServer(data);
        }
    }

    public void reset() {
        data = null;
        size = 0;
    }

    private static IChatComponent createClickableText(String text, String hovered, String command) {
        ChatComponentText clickableText = new ChatComponentText(text);
        clickableText.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(hovered)));
        clickableText.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return clickableText;
    }

    public void processCommand(String message) {
        if (message.startsWith("/germ-btn-click ")) {
            final String text = message.replace("/germ-btn-click ","");
            final String[] data = text.split(" ");
            try {
                switch (data[0].toLowerCase()) {
                    case "list": {
                        final String keyName = data[1];

                        if(games.containsKey(keyName)){
                            final List<GameItem> element = games.get(keyName);

                            final ChatComponentText style = new ChatComponentText("");
                            for (GameItem gameItem : element) {
                                style.appendText("   ");

                                final String itemName = gameItem.name;
                                style.appendSibling(createClickableText("[" + EnumChatFormatting.AQUA + itemName + EnumChatFormatting.RESET + "]", itemName, "/germ-btn-click play " + gameItem.id + " " + gameItem.gameId));
                            }

                            mc.thePlayer.addChatComponentMessage(style);
                        }

                        break;
                    }

                    case "play": {
                        final int num = Integer.parseInt(data[1]);
                        sendJoin(num, data[2]);

                        break;
                    }
                }
            } catch (Throwable e) {
                ChatUtil.display("参数错误！");
            }
        }
    }

    private void sendJoin(int num, String sid) {
        ByteBuf buf2 = Unpooled.buffer();
        buf2.writeBytes(OPEN_GUI);
        C17PacketCustomPayload packetIn1 = new C17PacketCustomPayload("germmod-netease", new PacketBuffer(buf2));
        Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacket(packetIn1);
        ByteBuf buf1 = Unpooled.buffer();
        byte[] bytes = buildJoinGamePacket(num, sid);
        buf1.writeBytes(bytes);
        C17PacketCustomPayload packetIn = new C17PacketCustomPayload("germmod-netease", new PacketBuffer(buf1));
        Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacket(packetIn);
        ++num;
    }

    private byte[] buildJoinGamePacket(int entry, String sid) {
        sid = "{\"entry\":" + entry + ",\"sid\":\"" + (String)sid + "\"}";
        byte[] bytes = new byte[JOIN_GAME.length + ((String)sid).getBytes().length + 2];
        System.arraycopy(JOIN_GAME, 0, bytes, 0, JOIN_GAME.length);
        bytes[JOIN_GAME.length] = (byte)(48 + entry);
        bytes[JOIN_GAME.length + 1] = (byte)((String)sid).length();
        System.arraycopy(((String)sid).getBytes(), 0, bytes, JOIN_GAME.length + 2, ((String)sid).getBytes().length);
        return bytes;
    }

    public GermWrapper() {
        final List<GameItem> gameItems = new ArrayList<>();
        gameItems.add(new GameItem(0, "BEDWAR/bw-dalu", "训练场"));
        gameItems.add(new GameItem(1, "BEDWAR/bw-solo", "8队单人 绝杀模式"));
        gameItems.add(new GameItem(2, "BEDWAR/bw-double", "8队双人 绝杀模式"));
        gameItems.add(new GameItem(3, "BEDWAR/bw-team", "4队4人 绝杀模式"));
        gameItems.add(new GameItem(4, "BEDWAR/bwxp16new", "无限火力16"));
        gameItems.add(new GameItem(5, "BEDWAR/bwxp-32", "无限火力32"));
        games.put("起床战争", gameItems);

        final List<GameItem> gameItems1 = new ArrayList<>();
        gameItems1.add(new GameItem(6, "SKYWAR/nskywar", "天空战争 单人"));
        gameItems1.add(new GameItem(7, "SKYWAR/nskywar-double", "天空战争 双人"));
        games.put("天空战争", gameItems1);

        final List<GameItem> gameItems2 = new ArrayList<>();
        gameItems2.add(new GameItem(8, "FIGHT/bihusuo", "悬土"));
        gameItems2.add(new GameItem(9, "FIGHT/pubg-kit", "吃鸡荒野"));
        gameItems2.add(new GameItem(10, "FIGHT/kb-game", "职业战争"));
        gameItems2.add(new GameItem(11, "FIGHT/arenaPVP", "竞技场（等级限制）"));
        gameItems2.add(new GameItem(12, "FIGHT/the-pit", "天坑之战"));
        games.put("个人竞技", gameItems2);

        final List<GameItem> gameItems3 = new ArrayList<>();
        gameItems3.add(new GameItem(13, "TEAM_FIGHT/csbwxp-32", "卡机起床"));
        gameItems3.add(new GameItem(14, "TEAM_FIGHT/bwkitxp-32", "职业无限火力起床"));
        gameItems3.add(new GameItem(15, "TEAM_FIGHT/anni", "核心战争"));
        gameItems3.add(new GameItem(16, "TEAM_FIGHT/battlewalls", "战墙"));
        gameItems3.add(new GameItem(17, "TEAM_FIGHT/skygiants", "巨人战争"));
        gameItems3.add(new GameItem(18, "TEAM_FIGHT/pubg-solo", "吃鸡单人"));
        games.put("团队竞技", gameItems3);

        final List<GameItem> gameItems4 = new ArrayList<>();
        gameItems4.add(new GameItem(19, "SURVIVE/oneblock", "单方块"));
        gameItems4.add(new GameItem(20, "SURVIVE/zskyblock", "天空岛"));
        gameItems4.add(new GameItem(21, "SURVIVE/zjyfy", "监禁风云"));
        gameItems4.add(new GameItem(22, "SURVIVE/xianjing", "仙境"));
        gameItems4.add(new GameItem(23, "SURVIVE/zuanshi", "钻石大陆"));
        gameItems4.add(new GameItem(24, "SURVIVE/zlysc", "龙域生存"));
        games.put("生存游戏", gameItems4);

        final List<GameItem> gameItems5 = new ArrayList<>();
        gameItems5.add(new GameItem(25, "LEISURE/tower", "守卫水晶"));
        gameItems5.add(new GameItem(26, "LEISURE/mg-game", "小游戏对战"));
        gameItems5.add(new GameItem(27, "LEISURE/sq-team", "抢羊大作战"));
        gameItems5.add(new GameItem(28, "LEISURE/stackgame", "叠叠乐"));
        gameItems5.add(new GameItem(29, "LEISURE/hp-game", "撸手山坡"));
        gameItems5.add(new GameItem(30, "LEISURE/ww-game", "狼人大作战"));
        games.put("休闲游戏", gameItems5);
    }
}
