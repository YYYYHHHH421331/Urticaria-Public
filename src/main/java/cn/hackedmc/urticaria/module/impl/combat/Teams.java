package cn.hackedmc.urticaria.module.impl.combat;

import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

@ModuleInfo(name = "module.combat.teams.name", description = "module.combat.teams.description", category = Category.COMBAT)
public final class Teams extends Module {
    public static Teams INSTANCE;
    private final BooleanValue armorValue = new BooleanValue("Armor Color", this,true);
    private final BooleanValue colorValue = new BooleanValue("Color",this, true);
    private final BooleanValue scoreboardValue = new BooleanValue("Scoreboard Team",this, true);
    public Teams(){
        INSTANCE = this;
    }
    public static boolean isSameTeam(Entity entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer)entity;
            if (!Teams.INSTANCE.isEnabled()) {
                return false;
            } else {
                return (Teams.INSTANCE.armorValue.getValue() && PlayerUtil.armorTeam(entityplayer)) || (Teams.INSTANCE.colorValue.getValue() && PlayerUtil.colorTeam(entityplayer)) || (Teams.INSTANCE.scoreboardValue.getValue() && PlayerUtil.scoreTeam(entityplayer));
            }
        } else {
            return false;
        }
    }
}