package cn.hackedmc.urticaria.module.impl.movement;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.impl.movement.sneak.GrimACSneak;
import cn.hackedmc.urticaria.module.impl.movement.sneak.HoldSneak;
import cn.hackedmc.urticaria.module.impl.movement.sneak.NCPSneak;
import cn.hackedmc.urticaria.module.impl.movement.sneak.StandardSneak;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.value.impl.ModeValue;

/**
 * @author Auth
 * @since 25/06/2022
 */
@Rise
@ModuleInfo(name = "module.movement.sneak.name", description = "module.movement.sneak.description", category = Category.MOVEMENT)
public class Sneak extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new StandardSneak("Standard", this))
            .add(new HoldSneak("Hold", this))
            .add(new NCPSneak("NCP", this))
            .add(new GrimACSneak("GrimAC", this))
            .setDefault("Standard");
}