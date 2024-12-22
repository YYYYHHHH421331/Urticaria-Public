package cn.hackedmc.urticaria.module.impl.movement;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.movement.noweb.*;
import cn.hackedmc.urticaria.value.impl.ModeValue;

@Rise
@ModuleInfo(name = "module.movement.noweb.name", description = "module.movement.noweb.description", category = Category.MOVEMENT)
public class NoWeb extends Module {
    public final ModeValue mode = new ModeValue("Mode", this)
            .add(new VanillaNoWeb("Vanilla", this))
            .add(new GrimACNoWeb("GrimAC", this))
            .setDefault("Vanilla");
}