package cn.hackedmc.urticaria.module.impl.other.timers;

import cn.hackedmc.urticaria.component.impl.player.BadPacketsComponent;
import cn.hackedmc.urticaria.component.impl.player.BlinkComponent;
import cn.hackedmc.urticaria.module.impl.other.Timer;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PostMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.TickEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.util.chat.ChatUtil;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.network.status.server.S01PacketPong;
import util.time.StopWatch;

import java.util.concurrent.LinkedBlockingQueue;

public class BalanceTimer extends Mode<Timer> {
    private final NumberValue speedValue = new NumberValue("Speed", this, 2, 1.5, 10, 0.1);
    private final BooleanValue autoDisable = new BooleanValue("Auto Close", this, true);
    private final BooleanValue debug = new BooleanValue("Debug", this, false);

    private double balance = 0;
    private final StopWatch stopWatch = new StopWatch();
    private final StopWatch debugTime = new StopWatch();
    private final StopWatch lastSend = new StopWatch();

    private final LinkedBlockingQueue<Packet<?>> serverPackets = new LinkedBlockingQueue<>();

    public BalanceTimer(String name, Timer parent) {
        super(name, parent);
    }

    @Override
    public void onDisable() {
        balance = 0;
        mc.timer.timerSpeed = 1f;
        if (mc.getNetHandler() == null || mc.isSingleplayer())
            serverPackets.clear();
        else
            while (!this.serverPackets.isEmpty()) {
                final Packet<?> packet = serverPackets.poll();

                if (packet instanceof C01PacketChatMessage) continue;

                mc.getNetHandler().addToReceiveQueueUnregistered(packet);
            }
    }

    @EventLink
    private final Listener<TickEvent> onTick = event -> {
        if (debug.getValue()) {
            if (debugTime.finished(500L)) {
                debugTime.reset();
                ChatUtil.display("Delayed packets -> %d; Fixed balance -> %.0f", serverPackets.size(), balance);
            }
        }

        if (MoveUtil.isMoving()) {
            if (balance <= 0 || serverPackets.isEmpty()) {
                if (autoDisable.getValue()) this.getParent().setEnabled(false);
                mc.timer.timerSpeed = 1.0f;
            } else {
                mc.timer.timerSpeed = speedValue.getValue().floatValue();
            }
        } else mc.timer.timerSpeed = 0.1f;
    };

    @EventLink(value = Priorities.HIGH)
    private final Listener<PostMotionEvent> onPostMotion = event -> {
        this.balance += stopWatch.getElapsedTime();
        this.stopWatch.reset();

        balance -= 50;

        while (!serverPackets.isEmpty()) {
            final Packet<?> packet = serverPackets.poll();

            if (packet instanceof C01PacketChatMessage) {
                break;
            }

            mc.getNetHandler().addToReceiveQueueUnregistered(packet);
        }

        if (MoveUtil.isMoving() && serverPackets.isEmpty() && autoDisable.getValue())
            this.getParent().setEnabled(false);
    };

    @Override
    public void onEnable() {
        serverPackets.clear();
        balance = 0;
        stopWatch.reset();
        debugTime.reset();
        lastSend.reset();
    }

    @EventLink
    private final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        final Packet<?> packet = event.getPacket();

        if (!(packet instanceof S00PacketKeepAlive || packet instanceof S02PacketChat || packet instanceof S01PacketPong)) {
            event.setCancelled();
            serverPackets.add(packet);
        }

        if (lastSend.finished(50L)) {
            serverPackets.add(new C01PacketChatMessage("release"));
            lastSend.reset();
        }
    };
}
