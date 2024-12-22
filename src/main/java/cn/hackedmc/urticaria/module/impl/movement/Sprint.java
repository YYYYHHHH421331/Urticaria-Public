package cn.hackedmc.urticaria.module.impl.movement;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.Type;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.StrafeEvent;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.impl.BooleanValue;

/**
 * @author Auth
 * @since 20/10/2021
 */
@ModuleInfo(name = "module.movement.sprint.name", description = "module.movement.sprint.description", category = Category.MOVEMENT)
public class Sprint extends Module {
    private final BooleanValue legit = new BooleanValue("Legit", this, true, () -> Client.CLIENT_TYPE != Type.BASIC);

    @EventLink(value = Priorities.LOW)
    public final Listener<StrafeEvent> onStrafe = event -> {

        mc.gameSettings.keyBindSprint.setPressed(true);

        if (Client.CLIENT_TYPE != Type.BASIC) return;

        if (mc.thePlayer.omniSprint && MoveUtil.isMoving() && !legit.getValue()) {
            mc.thePlayer.setSprinting(true);
        }

        mc.thePlayer.omniSprint = !legit.getValue() && MoveUtil.isMoving() && !mc.thePlayer.isCollidedHorizontally &&
                !mc.thePlayer.isSneaking() && !mc.thePlayer.isUsingItem();
    };

    @Override
    public void onDisable() {
        mc.thePlayer.setSprinting(mc.gameSettings.keyBindSprint.isKeyDown());
        mc.thePlayer.omniSprint = false;
    }
}