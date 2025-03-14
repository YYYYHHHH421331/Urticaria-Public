package cn.hackedmc.urticaria.module.impl.movement;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.movement.noslow.*;
import cn.hackedmc.urticaria.value.impl.ModeValue;

/**
 * @author Alan
 * @since 20/10/2021
 */
@Rise
@ModuleInfo(name = "module.movement.noslow.name", description = "module.movement.noslow.description", category = Category.MOVEMENT)
public class NoSlow extends Module {

    public final ModeValue mode = new ModeValue("Mode", this)
            .add(new VanillaNoSlow("Vanilla", this))
            .add(new NCPNoSlow("NCP", this))
            .add(new WatchdogNoSlow("Watchdog", this))
            .add(new NewNCPNoSlow("New NCP", this))
            .add(new CatACNoSlow("Cat Anti Cheat", this))
            .add(new BlinkNoSlow("Blink", this))
            .add(new BlocksMCNoSlow("BlocksMC", this))
            .add(new SpoofNoSlow("Spoof", this))
            .add(new IntaveNoSlow("Intave", this))
            .add(new MatrixNoSlow("Matrix", this))
            .add(new OldIntaveNoSlow("Old Intave", this))
            .add(new GrimACNoSlow("GrimAC", this))
            .add(new VariableNoSlow("Variable", this))
            .add(new PredictionNoSlow("Prediction", this))
            .setDefault("Vanilla");
}