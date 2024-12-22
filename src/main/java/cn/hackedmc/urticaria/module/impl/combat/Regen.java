package cn.hackedmc.urticaria.module.impl.combat;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.impl.combat.regen.HigherVersionRegen;
import cn.hackedmc.urticaria.module.impl.combat.regen.VanillaRegen;
import cn.hackedmc.urticaria.module.impl.combat.regen.WorldGuardRegen;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.value.impl.ModeValue;

@Rise
@ModuleInfo(name = "module.combat.regen.name", description = "module.combat.regen.description", category = Category.COMBAT)
public final class Regen extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new VanillaRegen("Vanilla", this))
            .add(new WorldGuardRegen("World Guard", this))
            .add(new HigherVersionRegen("1.17+", this))
            .setDefault("Vanilla");
}
