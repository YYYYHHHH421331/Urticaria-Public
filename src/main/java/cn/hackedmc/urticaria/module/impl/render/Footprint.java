package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.render.footprint.Body;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;

@ModuleInfo(name = "module.render.footprint.name", description = "module.render.footprint.description", category = Category.RENDER)
public class Footprint extends Module {
    public static Footprint INSTANCE;
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new Body("Body", this))
            .setDefault("Body");
    public final NumberValue amount = new NumberValue("Amount", this, 6, 1, 20, 1);

    public Footprint() {
        INSTANCE = this;
    }
}
