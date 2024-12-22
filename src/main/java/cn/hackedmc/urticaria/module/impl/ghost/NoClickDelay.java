package cn.hackedmc.urticaria.module.impl.ghost;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;

/**
 * @author Alan Jr. (Not Billionaire)
 * @since 19/9/2022
 */
@Rise
@ModuleInfo(name = "module.ghost.noclickdelay.name", description = "module.ghost.noclickdelay.description", category = Category.GHOST)
public class NoClickDelay extends Module  {

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (InstanceAccess.mc.thePlayer != null && InstanceAccess.mc.theWorld != null) {
            InstanceAccess.mc.leftClickCounter = 0;
        }
    };
}