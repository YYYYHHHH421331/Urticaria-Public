package net.minecraft.network;

import cn.hackedmc.urticaria.module.impl.exploit.Disabler;
import cn.hackedmc.urticaria.module.impl.exploit.disabler.GrimACDisabler;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.*;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.src.Config;
import net.minecraft.util.IThreadListener;

public class PacketThreadUtil {
    public static int lastDimensionId = Integer.MIN_VALUE;

    public static <T extends INetHandler> void checkThreadAndEnqueue(final Packet<T> packet, final T p_180031_1_, final IThreadListener p_180031_2_) throws ThreadQuickExitException {
        if (!p_180031_2_.isCallingFromMinecraftThread()) {
            p_180031_2_.addScheduledTask(new Runnable() {
                public void run() {
                    if (Disabler.INSTANCE != null && Disabler.INSTANCE.isEnabled() && Disabler.INSTANCE.grimAC.getValue() && Disabler.INSTANCE.grimACDisabler.usePost() && (
                            packet instanceof S32PacketConfirmTransaction ||
                                    packet instanceof S01PacketPong ||
                                    packet instanceof S18PacketEntityTeleport ||
                                    packet instanceof S12PacketEntityVelocity ||
                                    packet instanceof S27PacketExplosion ||
                                    packet instanceof S08PacketPlayerPosLook ||
                                    packet instanceof S23PacketBlockChange ||
                                    packet instanceof S22PacketMultiBlockChange ||
                                    packet instanceof S19PacketEntityStatus ||
                                    packet instanceof S06PacketUpdateHealth ||
                                    packet instanceof S05PacketSpawnPosition ||
                                    packet instanceof S2BPacketChangeGameState ||
                                    packet instanceof S01PacketJoinGame ||
                                    packet instanceof S38PacketPlayerListItem ||
                                    packet instanceof S39PacketPlayerAbilities ||
                                    packet instanceof S14PacketEntity ||
                                    packet instanceof S0FPacketSpawnMob ||
                                    packet instanceof S0CPacketSpawnPlayer ||
                                    packet instanceof S25PacketBlockBreakAnim ||
                                    packet instanceof S24PacketBlockAction ||
                                    packet instanceof S49PacketUpdateEntityNBT ||
                                    packet instanceof S47PacketPlayerListHeaderFooter ||
                                    packet instanceof S11PacketSpawnExperienceOrb ||
                                    packet instanceof S10PacketSpawnPainting ||
                                    packet instanceof S0DPacketCollectItem ||
                                    packet instanceof S1CPacketEntityMetadata ||
                                    packet instanceof S35PacketUpdateTileEntity ||
                                    packet instanceof S0EPacketSpawnObject ||
                                    packet instanceof S2CPacketSpawnGlobalEntity ||
                                    packet instanceof S13PacketDestroyEntities ||
                                    packet instanceof S30PacketWindowItems ||
                                    packet instanceof S2DPacketOpenWindow ||
                                    packet instanceof S04PacketEntityEquipment ||
                                    packet instanceof S2EPacketCloseWindow ||
                                    packet instanceof S3FPacketCustomPayload ||
                                    packet instanceof S28PacketEffect ||
                                    packet instanceof S1DPacketEntityEffect
                    )) {
                        Disabler.INSTANCE.grimACDisabler.packets.add((Packet<INetHandlerPlayClient>)packet);
                    } else {
                        PacketThreadUtil.clientPreProcessPacket(packet);
                        packet.processPacket(p_180031_1_);
                    }
                }
            });
            throw ThreadQuickExitException.field_179886_a;
        } else {
            clientPreProcessPacket(packet);
        }
    }

    protected static void clientPreProcessPacket(final Packet p_clientPreProcessPacket_0_) {
        if (p_clientPreProcessPacket_0_ instanceof S08PacketPlayerPosLook) {
            Config.getRenderGlobal().onPlayerPositionSet();
        }

        if (p_clientPreProcessPacket_0_ instanceof S07PacketRespawn) {
            final S07PacketRespawn s07packetrespawn = (S07PacketRespawn) p_clientPreProcessPacket_0_;
            lastDimensionId = s07packetrespawn.getDimensionID();
        } else if (p_clientPreProcessPacket_0_ instanceof S01PacketJoinGame) {
            final S01PacketJoinGame s01packetjoingame = (S01PacketJoinGame) p_clientPreProcessPacket_0_;
            lastDimensionId = s01packetjoingame.getDimension();
        } else {
            lastDimensionId = Integer.MIN_VALUE;
        }
    }
}
