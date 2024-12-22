package cn.hackedmc.urticaria.component.impl.player;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.Component;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.EntityList;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

@Rise
public final class GUIDetectionComponent extends Component {
    private static boolean userInterface;

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        userInterface = false;

        if (mc.currentScreen instanceof GuiChest) {
            final ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;

            int confidence = 0, totalSlots = 0, amount = 0;

            for (final Slot slot : container.inventorySlots) {
                if (slot.getHasStack() && amount++ <= 26 /* Amount of slots in a chest */) {
                    final ItemStack itemStack = slot.getStack();

                    if (itemStack == null) {
                        continue;
                    }

                    final String name = itemStack.getDisplayName();
                    final String expectedName = PlayerUtil.expectedName(itemStack);
                    final String strippedName = name.toLowerCase().replace(" ", "");
                    final String strippedExpectedName = expectedName.toLowerCase().replace(" ", "");

                    if (strippedName.contains(strippedExpectedName)) {
                        confidence -= 0.1;
                    } else {
                        confidence++;
                    }

                    totalSlots++;
                }
            }

            userInterface = (float) confidence / (float) totalSlots > 0.5f;
        } else {
            final ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(0);

            if (itemStack == null || !(itemStack.getItem() instanceof ItemBow)) {
                return;
            }

            final String name = itemStack.getDisplayName();
            final String expectedName = PlayerUtil.expectedName(itemStack);
            final String strippedName = name.toLowerCase().replace(" ", "");
            final String strippedExpectedName = expectedName.toLowerCase().replace(" ", "");

            if (!strippedName.contains(strippedExpectedName)) {
                userInterface = true;
            }
        }
    };

    public static boolean inGUI() {
        return userInterface;
    }
}
