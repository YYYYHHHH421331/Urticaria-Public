package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;

@Rise
@ModuleInfo(name = "module.render.musicplayer.name", description = "module.render.musicplayer.description", category = Category.RENDER)
public class MusicPlayer extends Module {
//    @Override
//    public void onEnable() {
//        getModule(ClickGUI.class).setEnabled(false);
//        Client.INSTANCE.getEventBus().register(Client.INSTANCE.getMusicMenu());
//        mc.displayGuiScreen(Client.INSTANCE.getMusicMenu());
//    }
//
//    @Override
//    protected void onDisable() {
//        Client.INSTANCE.getEventBus().unregister(Client.INSTANCE.getMusicMenu());
//        mc.displayGuiScreen(null);
//    }
}
