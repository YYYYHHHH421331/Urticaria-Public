package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.player.flagdetector.Flight;
import cn.hackedmc.urticaria.module.impl.player.flagdetector.Friction;
import cn.hackedmc.urticaria.module.impl.player.flagdetector.MathGround;
import cn.hackedmc.urticaria.module.impl.player.flagdetector.Strafe;
import cn.hackedmc.urticaria.util.chat.ChatUtil;
import cn.hackedmc.urticaria.value.impl.BooleanValue;

@Rise
@ModuleInfo(name = "module.player.flagdetector.name", description = "module.player.flagdetector.description", category = Category.PLAYER)
public class FlagDetector extends Module {
    public BooleanValue strafe = new BooleanValue("Strafe (Watchdog)", this, false, new Strafe("", this));
    public BooleanValue friction = new BooleanValue("Friction", this, false, new Friction("", this));
    public BooleanValue flight = new BooleanValue("Flight", this, false, new Flight("", this));
    public BooleanValue mathGround = new BooleanValue("Math Ground", this, false, new MathGround("", this));

    @Override
    protected void onEnable() {
        if (!Client.DEVELOPMENT_SWITCH) {
            ChatUtil.display("This module is only enabled for developers or config makersconfig");

            toggle();
        }
    }

    public void fail(String check) {
        ChatUtil.display("§7failed " + Client.INSTANCE.getThemeManager().getTheme().getChatAccentColor() + check);
    }
}
