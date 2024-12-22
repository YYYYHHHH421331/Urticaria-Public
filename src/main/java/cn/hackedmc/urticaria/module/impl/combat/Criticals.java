package cn.hackedmc.urticaria.module.impl.combat;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.impl.combat.criticals.*;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.value.impl.ModeValue;

@Rise
@ModuleInfo(name = "module.combat.criticals.name", description = "module.combat.criticals.description", category = Category.COMBAT)
public final class Criticals extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new PacketCriticals("Packet", this))
            .add(new EditCriticals("Edit", this))
            .add(new NoGroundCriticals("No Ground", this))
            .add(new HopCriticals("Hop", this))
            .add(new GrimACCriticals("GrimAC", this))
            .add(new NCPCriticals("NCP", this))
            .add(new WatchdogCriticals("Watchdog", this))
            .setDefault("Packet");
}
