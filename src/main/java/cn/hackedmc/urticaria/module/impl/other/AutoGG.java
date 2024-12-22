package cn.hackedmc.urticaria.module.impl.other;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.WorldChangeEvent;
import cn.hackedmc.urticaria.value.impl.StringValue;

@Rise
@ModuleInfo(name = "module.other.autogg.name", description = "module.other.autogg.description", category = Category.OTHER)
public final class AutoGG extends Module {
    private StringValue message = new StringValue("Message", this, "GG");
    private boolean active;

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.isSingleplayer() || mc.thePlayer.ticksExisted % 18 != 0 || mc.thePlayer.ticksExisted < 20 * 20 || !active || !mc.thePlayer.sendQueue.doneLoadingTerrain) return;

        if (mc.theWorld.playerEntities.stream().filter(entityPlayer -> !entityPlayer.isInvisible() && !entityPlayer.isSpectator()).count() <= 1) {
            active = false;

            mc.thePlayer.sendChatMessage(message.getValue());
        }
    };

    @EventLink()
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        active = true;
    };
}
