package cn.hackedmc.urticaria.newevent.impl.packet;

import cn.hackedmc.urticaria.newevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;

@Getter
@Setter
@AllArgsConstructor
public final class PacketSendEvent extends CancellableEvent {
    private Packet<?> packet;
}
