package cn.hackedmc.urticaria.module.impl.other;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.other.timers.BalanceTimer;
import cn.hackedmc.urticaria.module.impl.other.timers.NormalTimer;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.math.MathUtil;
import cn.hackedmc.urticaria.value.impl.BoundsNumberValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;

@Rise
@ModuleInfo(name = "module.other.timer.name", description = "module.other.timer.description", category = Category.OTHER)
public final class Timer extends Module {
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new NormalTimer("Normal", this))
            .add(new BalanceTimer("Balance", this))
            .setDefault("Normal");
}
