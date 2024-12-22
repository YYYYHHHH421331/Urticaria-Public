package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.impl.render.sessionstats.ModernSessionStats;
import cn.hackedmc.urticaria.module.impl.render.sessionstats.UrticariaSessionStats;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.value.impl.*;

/**
 * @author Hazsi
 * @since 10/13/2022
 */
@Rise
@ModuleInfo(name = "module.render.sessionstats.name", description = "module.render.sessionstats.description", category = Category.RENDER)
public class SessionStats extends Module {
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new ModernSessionStats("Modern", this))
            .add(new UrticariaSessionStats("Urticaria", this))
            .setDefault("Modern");

    public final DragValue position = new DragValue("", this, new Vector2d(200, 200), true);
}