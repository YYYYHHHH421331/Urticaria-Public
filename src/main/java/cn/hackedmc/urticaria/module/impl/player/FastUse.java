package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.player.fastuse.*;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;

@ModuleInfo(name = "module.player.fastuse.name", description = "module.player.fastuse.description", category = Category.PLAYER)
public class FastUse extends Module {
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new InstantFastUse("Instant", this))
            .add(new TimerFastUse("Timer", this))
            .add(new CustomFastUse("Custom", this))
            .add(new GrimACFastUse("GrimAC", this))
            .add(new NCPFastUse("NCP", this))
            .setDefault("Custom");
    public final BooleanValue food = new BooleanValue("Food", this, false);
    public final BooleanValue bow = new BooleanValue("Bow", this, false);
    public final BooleanValue autoShot = new BooleanValue("Auto Shot", this, false, () -> !bow.getValue());
}
