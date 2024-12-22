package cn.hackedmc.urticaria.module.impl.combat.antibot;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.module.impl.combat.AntiBot;
import cn.hackedmc.urticaria.value.Mode;

/**
 * @author Wykt
 * @since 2/04/2023
 */

public final class FuncraftAntiBot extends Mode<AntiBot> {
    public FuncraftAntiBot(String name, AntiBot parent) {
        super(name, parent);
    }

    @EventLink
    private final Listener<PreUpdateEvent> preUpdateEventListener = event -> {
        mc.theWorld.playerEntities.forEach(player -> {
            if(player.getDisplayName().getUnformattedText().contains("§")) {
                Client.INSTANCE.getBotManager().remove(player);
                return;
            }

            Client.INSTANCE.getBotManager().add(player);
        });
    };
}