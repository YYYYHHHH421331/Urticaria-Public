package cn.hackedmc.urticaria.module.impl.movement.flight;

import cn.hackedmc.urticaria.component.impl.player.BlinkComponent;
import cn.hackedmc.urticaria.component.impl.player.SlotComponent;
import cn.hackedmc.urticaria.module.impl.movement.Flight;
import cn.hackedmc.urticaria.module.impl.player.Blink;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.other.TickEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.util.chat.ChatUtil;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;

/**
 * @author Alan
 * @since 03.07.2022
 */
public class GrimFlight extends Mode<Flight> {

    public GrimFlight(String name, Flight parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        BlinkComponent.blinking = true;
        ChatUtil.display("Any blocks you place will be ghost blocks.");
    }

    @Override
    public void onDisable() {
        if (!getModule(Blink.class).isEnabled())
            BlinkComponent.blinking = false;
    }

    @EventLink
    private final Listener<TickEvent> onTick = event -> {
        while (BlinkComponent.packets.size() >= 40) {
            PacketUtil.sendNoEvent(BlinkComponent.packets.poll());
        }
    };

    @EventLink
    private final Listener<PacketSendEvent> onPacketSend = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C08PacketPlayerBlockPlacement) {
            final C08PacketPlayerBlockPlacement placement = (C08PacketPlayerBlockPlacement) packet;

            if (!placement.getPosition().equals(new BlockPos(-1, -1, -1))) {
                event.setCancelled(true);
                if (!BlinkComponent.blinking) {
                    PacketUtil.sendNoEvent(new C0EPacketClickWindow(0, 9, SlotComponent.getItemIndex(), 2, null, (short) 0));
                    PacketUtil.sendNoEvent(placement);
                    PacketUtil.sendNoEvent(new C0EPacketClickWindow(0, 9, SlotComponent.getItemIndex(), 2, null, (short) 0));
                } else {
                    BlinkComponent.packets.add(new C0EPacketClickWindow(0, 9, SlotComponent.getItemIndex(), 2, null, (short) 0));
                    BlinkComponent.packets.add(placement);
                    BlinkComponent.packets.add(new C0EPacketClickWindow(0, 9, SlotComponent.getItemIndex(), 2, null, (short) 0));
                }
            }
        }
    };
}
