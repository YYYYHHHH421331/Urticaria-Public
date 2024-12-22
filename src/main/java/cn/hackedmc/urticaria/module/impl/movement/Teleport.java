package cn.hackedmc.urticaria.module.impl.movement;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.movement.teleport.WatchdogTeleport;
import cn.hackedmc.urticaria.value.impl.ModeValue;

/**
 * @author Alan
 * @since 18/11/2022
 */

@Rise
@ModuleInfo(name = "module.movement.teleport.name", description = "module.movement.teleport.description", category = Category.MOVEMENT)
public class Teleport extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new WatchdogTeleport("Watchdog", this))
            .setDefault("Vanilla");

}