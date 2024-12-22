package cn.hackedmc.urticaria.module.impl.ghost;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.player.SlotComponent;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import net.minecraft.item.ItemBlock;

/**
 * @author Alan
 * @since 29/01/2021
 */

@Rise
@ModuleInfo(name = "module.ghost.fastplace.name", description = "module.ghost.fastplace.description", category = Category.GHOST)
public class FastPlace extends Module {

    private final NumberValue delay = new NumberValue("Delay", this, 0, 0, 3, 1);

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (SlotComponent.getItemStack() != null && SlotComponent.getItemStack().getItem() instanceof ItemBlock) {
            mc.rightClickDelayTimer = Math.min(mc.rightClickDelayTimer, this.delay.getValue().intValue());
        }
    };
}