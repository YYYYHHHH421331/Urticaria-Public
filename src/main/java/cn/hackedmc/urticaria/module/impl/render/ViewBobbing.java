package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.render.ViewBobbingEvent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.SubMode;

@ModuleInfo(name = "module.render.viewbobbing.name", description = "module.render.viewbobbing.description", category = Category.RENDER)
public final class ViewBobbing extends Module {

    public final ModeValue viewBobbingMode = new ModeValue("Mode", this)
            .add(new SubMode("Smooth"))
            .add(new SubMode("Meme"))
            .add(new SubMode("None"))
            .setDefault("None");

    @EventLink()
    public final Listener<ViewBobbingEvent> onViewBobbing = event -> {
        if (viewBobbingMode.getValue().getName().equals("Smooth") && (event.getTime() == 0 || event.getTime() == 2)) {
            event.setCancelled(true);
        }
    };

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        mc.gameSettings.viewBobbing = true;

        switch (viewBobbingMode.getValue().getName()) {
            case "Meme": {
                mc.thePlayer.cameraYaw = 0.5F;
                break;
            }

            case "None": {
                mc.thePlayer.distanceWalkedModified = 0.0F;
                break;
            }
        }
    };
}