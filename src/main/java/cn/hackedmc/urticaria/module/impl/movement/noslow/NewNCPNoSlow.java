package cn.hackedmc.urticaria.module.impl.movement.noslow;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.component.impl.player.BadPacketsComponent;
import cn.hackedmc.urticaria.component.impl.player.SlotComponent;
import cn.hackedmc.urticaria.module.impl.combat.KillAura;
import cn.hackedmc.urticaria.module.impl.movement.NoSlow;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.SlowDownEvent;
import cn.hackedmc.urticaria.newevent.impl.other.TeleportEvent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.component.impl.render.notificationcomponent.NotificationComponent;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

import java.util.Objects;

/**
 * @author Auth
 * @since 18/11/2021
 */

public class NewNCPNoSlow extends Mode<NoSlow> {

    private int disable;

    public NewNCPNoSlow(String name, NoSlow parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        this.disable++;
        if (InstanceAccess.mc.thePlayer.isUsingItem() && this.disable > 10 && !BadPacketsComponent.bad(false,
                true, true, false, false) && !(Objects.requireNonNull(SlotComponent.getItemStack()).getItem() instanceof ItemBow)  && Client.INSTANCE.getModuleManager().get(KillAura.class).target == null) {
            PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex() % 8 + 1));
            PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex()));
        }
    };

    @EventLink
    public final Listener<SlowDownEvent> onSlowDown = event -> {
        if(Client.INSTANCE.getModuleManager().get(KillAura.class).target == null) event.setCancelled(true);
    };


    @EventLink()
    public final Listener<TeleportEvent> onTeleport = event -> {
        this.disable = 0;
    };
    @Override
    public void onEnable() {
        NotificationComponent.post("Credit", "Thanks Auth for this No Slow", 5000);
    }
}