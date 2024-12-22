package cn.hackedmc.urticaria.module.impl.ghost;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.player.SlotComponent;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import net.minecraft.item.ItemBlock;

/**
 * @author Alan
 * @since 29/01/2021
 */

@Rise
@ModuleInfo(name = "module.ghost.safewalk.name", description = "module.ghost.safewalk.description", category = Category.GHOST)
public class SafeWalk extends Module {

    private final BooleanValue blocksOnly = new BooleanValue("Blocks Only", this, false);
    private final BooleanValue backwardsOnly = new BooleanValue("Backwards Only", this, false);

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {

        mc.thePlayer.safeWalk = mc.thePlayer.onGround && (!mc.gameSettings.keyBindForward.isKeyDown() || !backwardsOnly.getValue()) &&
                ((SlotComponent.getItemStack() != null && SlotComponent.getItemStack().getItem() instanceof ItemBlock) ||
                        !this.blocksOnly.getValue());
    };
}