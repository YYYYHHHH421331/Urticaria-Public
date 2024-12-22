package cn.hackedmc.urticaria.module.impl.combat.criticals;

import cn.hackedmc.urticaria.module.impl.combat.Criticals;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.other.AttackEvent;
import cn.hackedmc.urticaria.value.Mode;

public class HopCriticals extends Mode<Criticals> {
    public HopCriticals(String name, Criticals parent) {
        super(name, parent);
    }

    @EventLink
    private final Listener<AttackEvent> onAttack = event -> {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.08;
        }
    };
}
