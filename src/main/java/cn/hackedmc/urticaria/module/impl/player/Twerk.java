package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.input.MoveInputEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;

@Rise
@ModuleInfo(name = "module.player.twerk.name", description = "module.player.twerk.description", category = Category.PLAYER)
public class Twerk extends Module {

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.gameSettings.keyBindSneak.setPressed(Math.random() < 0.5 && mc.thePlayer.ticksExisted % 2 == 0);
    };


    @EventLink()
    public final Listener<MoveInputEvent> onMove = event -> {
        event.setSneakSlowDownMultiplier(0);
    };
}