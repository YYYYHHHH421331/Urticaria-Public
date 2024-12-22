package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.player.SlotComponent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

@Rise
@ModuleInfo(name = "module.player.autosoup.name", description = "module.player.autosoup.description", category = Category.PLAYER)
public class AutoSoup extends Module {
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Legit"))
            .add(new SubMode("Fast"))
            .setDefault("Legit");
    private final NumberValue health = new NumberValue("Health", this, 15, 1, 20, 1);

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (mc.thePlayer.getHealth() <= health.getValue().floatValue()) {
            for (int i = 0; i < 9; i++) {
                if (mc.thePlayer.getHealth() > health.getValue().floatValue())
                    break;

                final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

                if (stack == null)
                    continue;

                if (stack.getItem() instanceof ItemSoup) {
                    if (mode.getValue().getName().equalsIgnoreCase("Legit")) {
                        SlotComponent.setSlot(i);

                        PacketUtil.send(new C08PacketPlayerBlockPlacement(SlotComponent.getItemStack()));
                    } else {
                        PacketUtil.send(new C09PacketHeldItemChange(i));
                        PacketUtil.send(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(i)));
                        PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex()));
                    }
                }
            }
        }
    };
}
