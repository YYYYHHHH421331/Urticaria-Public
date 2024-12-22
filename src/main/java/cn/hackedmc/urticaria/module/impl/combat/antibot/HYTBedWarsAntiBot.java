package cn.hackedmc.urticaria.module.impl.combat.antibot;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.module.impl.combat.AntiBot;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.WorldChangeEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HYTBedWarsAntiBot extends Mode<AntiBot> {
    public HYTBedWarsAntiBot(String name, AntiBot parent){
        super(name,parent);
    }
    private final ModeValue bedWarsMode = new ModeValue("BedWarsMode",this)
            .add(new SubMode("4v4/1v1"))
            .add(new SubMode("32/64"))
            .add(new SubMode("16v16"))
            .add(new SubMode("Work"))
            .setDefault("4v4/1v1");

    private final List<String> playerName = new ArrayList<>();

    private final Pattern pattern1 = Pattern.compile("杀死了\\s+(.*?)\\(");
    private final Pattern pattern2 = Pattern.compile("起床战争>>\\s+(.*?)\\s+\\(((.*?)死了!)\\)");
    private final Pattern pattern3 = Pattern.compile("击败了\\s+(.*?)!");
    private final Pattern pattern4 = Pattern.compile("玩家\\s+(.*?)死了！");
    private final Pattern pattern5 = Pattern.compile(">>(.*?)\\s+被");
    private final Pattern pattern6 = Pattern.compile("击杀了(.*?)\\s*!");
    private final Pattern pattern7 = Pattern.compile(">>(.*?)\\s+Boom！！!");

    @EventLink
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        mc.theWorld.playerEntities.forEach(player -> {
            if (playerName.contains(player.getCommandSenderName()))
                Client.INSTANCE.getBotManager().add(player);
            else
                Client.INSTANCE.getBotManager().remove(player);
        });
    };

    @EventLink
    private final Listener<WorldChangeEvent> onWorld = event -> {
        this.playerName.clear();
    };

    @EventLink()
    private final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        Packet<?> packet = event.getPacket();

        if (packet instanceof S02PacketChat) {
            S02PacketChat sPacketChat = (S02PacketChat) packet;
            if (sPacketChat.chatComponent == null || sPacketChat.chatComponent.getUnformattedText() == null) return;
            String unformattedText = sPacketChat.chatComponent.getUnformattedText();

            if (unformattedText.contains("获得胜利!") || unformattedText.contains("游戏开始 ...")) {
                this.playerName.clear();
            }

            switch (bedWarsMode.getValue().getName().toLowerCase()) {
                case "4v4/1v1":
                case "32/64":
                    Matcher matcher1 = pattern1.matcher(unformattedText);
                    Matcher matcher2 = pattern2.matcher(unformattedText);

                    if (matcher1.find() && !(unformattedText.contains(": 起床战争>>") || unformattedText.contains(": 杀死了"))) {
                        String name = matcher1.group(1).trim();
                        if (!name.isEmpty()) {
                            playerName.add(name);
                            new Thread(() -> {
                                try {
                                    Thread.sleep(6000);
                                    playerName.remove(name);
                                } catch (InterruptedException ignored) {}
                            }).start();
                        }
                    }

                    if (matcher2.find() && !(unformattedText.contains(": 起床战争>>") || unformattedText.contains(": 杀死了"))) {
                        String name = matcher2.group(1).trim();
                        if (!name.isEmpty()) {
                            playerName.add(name);
                            new Thread(() -> {
                                try {
                                    Thread.sleep(6000);
                                    playerName.remove(name);
                                } catch (InterruptedException ignored) {}
                            }).start();
                        }
                    }
                    break;

                case "16v16":
                    Matcher matcher3 = pattern3.matcher(unformattedText);
                    Matcher matcher4 = pattern4.matcher(unformattedText);

                    if (matcher3.find() && !(unformattedText.contains(": 击败了") || unformattedText.contains(": 玩家 "))) {
                        String name = matcher3.group(1).trim();
                        if (!name.isEmpty()) {
                            playerName.add(name);
                            new Thread(() -> {
                                try {
                                    Thread.sleep(10000);
                                    playerName.remove(name);
                                } catch (InterruptedException ignored) {}
                            }).start();
                        }
                    }

                    if (matcher4.find() && !(unformattedText.contains(": 击败了") || unformattedText.contains(": 玩家 "))) {
                        String name = matcher4.group(1).trim();
                        if (!name.isEmpty()) {
                            playerName.add(name);
                            new Thread(() -> {
                                try {
                                    Thread.sleep(10000);
                                    playerName.remove(name);
                                } catch (InterruptedException ignored) {}
                            }).start();
                        }
                    }
                    break;

                case "work":
                    Matcher matcher5 = pattern5.matcher(unformattedText);
                    Matcher matcher6 = pattern6.matcher(unformattedText);
                    Matcher matcher7 = pattern7.matcher(unformattedText);

                    if (matcher5.find() && !(unformattedText.contains(": 击杀了") || unformattedText.contains(": 玩家 "))) {
                        String name = matcher5.group(1).trim();
                        if (!name.isEmpty()) {
                            playerName.add(name);
                            new Thread(() -> {
                                try {
                                    Thread.sleep(10000);
                                    playerName.remove(name);
                                } catch (InterruptedException ignored) {}
                            }).start();
                        }
                    }

                    if (matcher6.find() && !(unformattedText.contains(": 击杀了") || unformattedText.contains(": 玩家 "))) {
                        String name = matcher6.group(1).trim();
                        if (!name.isEmpty()) {
                            playerName.add(name);
                            new Thread(() -> {
                                try {
                                    Thread.sleep(10000);
                                    playerName.remove(name);
                                } catch (InterruptedException ignored) {}
                            }).start();
                        }
                    }

                    if (matcher7.find() && !(unformattedText.contains(": Boom") || unformattedText.contains(": 玩家 "))) {
                        String name = matcher7.group(1).trim();
                        if (!name.isEmpty()) {
                            playerName.add(name);
                            new Thread(() -> {
                                try {
                                    Thread.sleep(10000);
                                    playerName.remove(name);
                                } catch (InterruptedException ignored) {}
                            }).start();
                        }
                    }

                    break;
            }
        }
    };
}
