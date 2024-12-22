package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.value.impl.NumberValue;

@ModuleInfo(name = "module.render.nofov.name", description = "module.render.nofov.description", category = Category.RENDER)
public class NoFOV extends Module {
    public final NumberValue fov = new NumberValue("FOV", this, 1.0, 1.0, 1.5, 0.01);

    public static NoFOV INSTANCE;

    public NoFOV() {
        INSTANCE = this;
    }
}
