package cn.hackedmc.urticaria.module.impl.other.protocols.hyt.forge;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

public class ForgeChannel {
    public FMLHandshakeClientState currentState;

    public void processForge(S3FPacketCustomPayload payload) {
        final PacketBuffer buffer = payload.getBufferData();
        this.currentState.accept(buffer.readByte(), buffer, s -> {
            this.currentState = s;
        });
    }
}
