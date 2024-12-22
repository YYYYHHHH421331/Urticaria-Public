package cn.hackedmc.urticaria.command.impl;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.command.Command;
import cn.hackedmc.urticaria.util.chat.ChatUtil;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * @author Auth
 * @since 3/07/2022
 */
@Rise
public final class Stuck extends Command {

    public Stuck() {
        super("command.stuck.description", "stuck");
    }

    @Override
    public void execute(final String[] args) {
        PacketUtil.sendNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(InstanceAccess.mc.thePlayer.posX, -1, InstanceAccess.mc.thePlayer.posZ, false));
        ChatUtil.display("command.stuck.sent");
    }
}