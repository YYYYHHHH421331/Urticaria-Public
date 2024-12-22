package cn.hackedmc.urticaria.module.impl.movement.phase;

import cn.hackedmc.urticaria.module.impl.movement.Phase;
import cn.hackedmc.urticaria.value.Mode;

public class ClipPhase extends Mode<Phase> {
    public ClipPhase(String name, Phase parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ);
    }
}
