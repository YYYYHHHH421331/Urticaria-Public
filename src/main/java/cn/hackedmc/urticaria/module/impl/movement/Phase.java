package cn.hackedmc.urticaria.module.impl.movement;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.movement.phase.ClipPhase;
import cn.hackedmc.urticaria.module.impl.movement.phase.HuaYuTingPhase;
import cn.hackedmc.urticaria.module.impl.movement.phase.NormalPhase;
import cn.hackedmc.urticaria.value.impl.ModeValue;

/**
 * @author Alan
 * @since 20/10/2021
 */

@Rise
@ModuleInfo(name = "module.movement.phase.name", description = "module.movement.phase.description", category = Category.MOVEMENT)
public class Phase extends Module {
    public static Phase INSTANCE;

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new NormalPhase("Normal", this))
            .add(new HuaYuTingPhase("HuaYuTing", this))
            .add(new ClipPhase("Clip", this))
            .setDefault("Normal");

    public Phase() {
        INSTANCE = this;
    }
}