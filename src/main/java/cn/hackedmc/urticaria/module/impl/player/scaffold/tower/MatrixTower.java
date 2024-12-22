package cn.hackedmc.urticaria.module.impl.player.scaffold.tower;

import cn.hackedmc.urticaria.module.impl.player.Scaffold;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.value.Mode;

public class MatrixTower extends Mode<Scaffold> {

    public MatrixTower(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.gameSettings.keyBindJump.isKeyDown() && PlayerUtil.isBlockUnder(2, false) && mc.thePlayer.motionY < 0.2) {
            mc.thePlayer.motionY = 0.42F;
            event.setOnGround(true);
        }
    };
}
