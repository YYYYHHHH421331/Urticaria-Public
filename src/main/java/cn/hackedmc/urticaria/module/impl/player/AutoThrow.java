package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.movement.NoSlow;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import util.time.StopWatch;

@ModuleInfo(name = "module.player.autothrow.name", description = "module.player.autothrow.description", category = Category.PLAYER)
public class AutoThrow extends Module {
    private final StopWatch stopWatch = new StopWatch();

    @EventLink
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mc.thePlayer.getHeldItem() == null) return;
        final NoSlow noSlow = getModule(NoSlow.class);

        final int throwable = getThrowable();
        if (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && mc.thePlayer.isUsingItem()) {
            if (noSlow.isEnabled() && noSlow.mode.getValue().getName().equalsIgnoreCase("grimac")) {
                if (throwable == -1 || !stopWatch.finished(300L)) {
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                } else {
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(throwable));
                    mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(throwable)));
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    stopWatch.reset();
                }
            } else {
                if (throwable != -1 && stopWatch.finished(1000L)) {
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(throwable));
                    mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(throwable)));
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    stopWatch.reset();
                }
            }
        }
    };

    private static int getThrowable() {
        for (int i = 0;i < 9;i++) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

            if (stack == null)
                continue;

            if (stack.getItem() instanceof ItemEgg || stack.getItem() instanceof ItemSnowball) {
                return i;
            }
        }

        return -1;
    }
}
