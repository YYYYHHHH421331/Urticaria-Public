package cn.hackedmc.urticaria.module.impl.movement.noweb;

import cn.hackedmc.urticaria.module.impl.movement.NoWeb;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.value.Mode;

public class VanillaNoWeb extends Mode<NoWeb> {
    public VanillaNoWeb(String name, NoWeb parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> mc.thePlayer.isInWeb = false;
}