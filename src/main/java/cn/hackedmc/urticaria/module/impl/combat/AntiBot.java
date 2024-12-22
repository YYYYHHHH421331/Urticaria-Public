package cn.hackedmc.urticaria.module.impl.combat;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.impl.combat.antibot.*;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.value.impl.BooleanValue;

@Rise
@ModuleInfo(name = "module.combat.antibot.name", description = "module.combat.antibot.description", category = Category.COMBAT)
public final class AntiBot extends Module {
    private final BooleanValue advancedAntiBot = new BooleanValue("Always Nearby Check", this, false,
            new AdvancedAntiBot("", this));

    private final BooleanValue watchdogAntiBot = new BooleanValue("Watchdog Check", this, false,
            new WatchdogAntiBot("", this));

    private final BooleanValue funcraftAntiBot = new BooleanValue("Funcraft Check", this, false,
            new FuncraftAntiBot("", this));

    private final BooleanValue hytAntiBot = new BooleanValue("HYT BedWars Check", this, false,
            new HYTBedWarsAntiBot("", this));
    private final BooleanValue ab = new BooleanValue("HYT Ground Check", this, false,
            new HytGroundAntiBot("", this));
    private final BooleanValue ncps = new BooleanValue("NPC Detection Check", this, false,
            new NPCAntiBot("", this));

    private final BooleanValue middleClick = new BooleanValue("Middle Click Bot", this, false,
            new MiddleClickBot("", this));

    @Override
    protected void onDisable() {
        Client.INSTANCE.getBotManager().clear();
    }
}
