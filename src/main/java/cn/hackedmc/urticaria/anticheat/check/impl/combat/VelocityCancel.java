package cn.hackedmc.urticaria.anticheat.check.impl.combat;

import cn.hackedmc.urticaria.anticheat.check.Check;
import cn.hackedmc.urticaria.anticheat.check.api.CheckInfo;
import cn.hackedmc.urticaria.anticheat.data.PlayerData;
import net.minecraft.network.Packet;

// Code for this check is in the flight prediction check
@CheckInfo(name = "Velocity", type = "Cancel", description = "Detects velocities")
public final class VelocityCancel extends Check {

    public VelocityCancel(final PlayerData data) {
        super(data);
    }

    @Override
    public void handle(final Packet<?> packet) {
    }
}