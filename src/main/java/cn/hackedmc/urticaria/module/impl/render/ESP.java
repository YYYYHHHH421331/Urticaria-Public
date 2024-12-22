package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.impl.render.esp.ChamsESP;
import cn.hackedmc.urticaria.module.impl.render.esp.GlowESP;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.value.impl.BooleanValue;

@Rise
@ModuleInfo(name = "module.render.esp.name", description = "module.render.esp.description", category = Category.RENDER)
public final class ESP extends Module {

    private BooleanValue glowESP = new BooleanValue("Glow", this, false, new GlowESP("", this));
    private BooleanValue chamsESP = new BooleanValue("Chams", this, false, new ChamsESP("", this));

}
