package cn.hackedmc.urticaria.module.impl.combat;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.player.SlotComponent;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.AttackEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

@Rise
@ModuleInfo(name = "module.combat.comboonehit.name", description = "module.combat.comboonehit.description", category = Category.COMBAT)
public final class ComboOneHit extends Module {
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Attack Packet"))
            .add(new SubMode("Switch Item"))
            .setDefault("Attack Packet");

    private final NumberValue switchTimes = new NumberValue("Switch Times", this, 10, 1, 100, 1, () -> !mode.getValue().getName().equalsIgnoreCase("Switch Item"));
    public final NumberValue packets = new NumberValue("Attack Packets", this, 50, 1, 1000, 25, () -> !mode.getValue().getName().equalsIgnoreCase("Attack Packet"));
    private int attackTick = 0;

    @Override
    protected void onEnable() {
        attackTick = 0;
    }

    @EventLink()
    public final Listener<AttackEvent> onAttack = event -> {
        if (mode.getValue().getName().equalsIgnoreCase("Attack Packet")) {
            for (int i = 0; i < packets.getValue().intValue(); i++) {
                PacketUtil.send(new C02PacketUseEntity(event.getTarget(), C02PacketUseEntity.Action.ATTACK));
            }
        } else {
            attackTick = 3;
        }
    };

    @EventLink
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mode.getValue().getName().equalsIgnoreCase("Switch Item") && attackTick > 0) {
            attackTick--;
            for (int i = 0;i < switchTimes.getValue().intValue();i++) {
                PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex() % 8 + 1));
                PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex()));
            }
        }
    };
}
