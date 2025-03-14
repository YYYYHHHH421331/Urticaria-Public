package cn.hackedmc.urticaria.module.impl.combat;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.impl.combat.velocity.*;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;

@Rise
@ModuleInfo(name = "module.combat.velocity.name", description = "module.combat.velocity.description" /* Sorry, Tecnio. */ /* Sorry Hazsi. */, category = Category.COMBAT)
public final class Velocity extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new StandardVelocity("Standard", this))
            .add(new BufferAbuseVelocity("Buffer Abuse", this))
            .add(new DelayVelocity("Delay", this))
            .add(new LegitVelocity("Legit", this))
            .add(new GroundVelocity("Ground", this))
            .add(new IntaveVelocity("Intave", this))
            .add(new MatrixVelocity("Matrix", this))
            .add(new AACVelocity("AAC", this))
            .add(new GrimACVelocity("GrimAC", this))
            .add(new VulcanVelocity("Vulcan", this))
            .add(new RedeskyVelocity("Redesky", this))
            .add(new TickVelocity("Tick", this))
            .add(new BounceVelocity("Bounce", this))
            .add(new KarhuVelocity("Karhu", this))
            .add(new MMCVelocity("MMC", this))
            .add(new UniversoCraftVelocity("Universocraft", this))
            .add(new WatchdogVelocity("Watchdog", this))
            .setDefault("Standard");

    public final BooleanValue onSwing = new BooleanValue("On Swing", this, false);
    public final BooleanValue onSprint = new BooleanValue("On Sprint", this, false);
}
