/*
 * This file is part of ViaLoadingBase - https://github.com/FlorianMichael/ViaLoadingBase
 * Copyright (C) 2023 FlorianMichael/EnZaXD and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.minecraft.viamcp.network;

import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import com.viaversion.viabackwards.protocol.protocol1_16_4to1_17.Protocol1_16_4To1_17;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelDecoderException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.protocols.protocol1_17_1to1_17.ClientboundPackets1_17_1;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ServerboundPackets1_17;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.viamcp.ViaMCP;

import java.util.List;

@ChannelHandler.Sharable
public class VLBViaDecodeHandler extends MessageToMessageDecoder<ByteBuf> {
    private final UserConnection user;

    public VLBViaDecodeHandler(UserConnection user) {
        this.user = user;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> out) throws Exception {
        if (user == null) return;
        if (!user.checkIncomingPacket()) throw CancelDecoderException.generate(null);
        if (!user.shouldTransformPacket()) {
            out.add(bytebuf.retain());
            return;
        }

        ByteBuf raw = bytebuf.copy();

        boolean cancel = false;

        //1.17 fix by fyxar
        if(ViaMCP.getInstance().getVersion() >= 755) {
            try {
                if (Type.VAR_INT.read(raw) == ClientboundPackets1_17_1.PING.getId()) {
                    int id = Type.INT.read(raw);

                    InstanceAccess.mc.addScheduledTask(() -> {
                        PacketWrapper wrapper = PacketWrapper.create(ServerboundPackets1_17.PONG, user);
                        wrapper.write(Type.INT, id);
                        try {
                            wrapper.sendToServer(Protocol1_16_4To1_17.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    cancel = true;
                }
            } finally {
                raw.release();
            }
        }

        if(cancel) return;

        ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(bytebuf);
        try {
            if (transformedBuf == null) return;
            user.transformIncoming(transformedBuf, CancelDecoderException::generate);

            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (PipelineUtil.containsCause(cause, CancelCodecException.class)) return;

        if ((PipelineUtil.containsCause(cause, InformativeException.class)
                && user.getProtocolInfo().getState() != State.HANDSHAKE)
                || Via.getManager().debugHandler().enabled()) {
            cause.printStackTrace();
        }
    }
}