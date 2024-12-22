package cn.hackedmc.urticaria.module.impl.combat.antibot;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.module.impl.combat.AntiBot;
import cn.hackedmc.urticaria.value.Mode;

public final class AdvancedAntiBot extends Mode<AntiBot> {

    public AdvancedAntiBot(String name, AntiBot parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        mc.theWorld.playerEntities.forEach(player -> {
            if (mc.thePlayer.getDistanceSq(player.posX, mc.thePlayer.posY, player.posZ) > 200) {
                Client.INSTANCE.getBotManager().remove(player);
            }

            if (player.ticksExisted < 5 || player.isInvisible() || mc.thePlayer.getDistanceSq(player.posX, mc.thePlayer.posY, player.posZ) > 100 * 100) {
                Client.INSTANCE.getBotManager().add(player);
            }
        });
    };

}