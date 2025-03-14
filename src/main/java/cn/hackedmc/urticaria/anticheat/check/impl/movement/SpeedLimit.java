package cn.hackedmc.urticaria.anticheat.check.impl.movement;

import cn.hackedmc.urticaria.anticheat.check.Check;
import cn.hackedmc.urticaria.anticheat.check.api.CheckInfo;
import cn.hackedmc.urticaria.anticheat.data.PlayerData;
import cn.hackedmc.urticaria.anticheat.util.PacketUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import util.time.StopWatch;

@CheckInfo(name = "Speed", type = "Limit", description = "Detects speeds")
public final class SpeedLimit extends Check {

    private double combinedMove, maxMove;
    private StopWatch timeOnServer = new StopWatch();

    public SpeedLimit(final PlayerData data) {
        super(data);
    }

    @Override
    public void handle(final Packet<?> packet) {
        if (((PacketUtil.isRelMove(packet) && ((S14PacketEntity) packet).entityId == data.getPlayer().getEntityId()) ||
                (packet instanceof S18PacketEntityTeleport && ((S18PacketEntityTeleport) packet).entityId == data.getPlayer().getEntityId()))) {

            EntityOtherPlayerMP entity = data.getPlayer();

            // This prevents WatchDog bots from creating false positives
            if (entity.isInvisible()) return;

            if (data.getTicksSinceLastVelocity() <= 10 + data.getMaxPingSpike()) return;

            double speedPerTick = 0.356;
            double deltaYaw = Math.abs(data.getYaw() - data.getLastYaw());

            if (!data.isMovingForward()) speedPerTick *= 0.7;
            else if (data.getTicksSinceAttack() <= 2) speedPerTick *= 0.75;
            if (deltaYaw < 180 && deltaYaw > 20 && !data.isOnGround()) speedPerTick *= 1 - deltaYaw / 300;
            if (data.getPlayer().isBlocking()) speedPerTick *= 0.7;

            combinedMove += data.getDeltaXZ();
            maxMove += (timeOnServer.getElapsedTime() / 50f) * speedPerTick;
            maxMove = Math.min(maxMove, combinedMove + 2);

            if (combinedMove > maxMove) {
                if (getBuffer() > 4) fail();

                increaseBuffer();
                maxMove = combinedMove;
            } else {
                decreaseBufferBy(1 / 5f);
            }

            timeOnServer.reset();
        }
    }
}