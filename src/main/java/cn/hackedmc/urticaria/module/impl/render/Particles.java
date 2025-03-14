package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.impl.other.AttackEvent;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;


/**
 * @author Alan
 * @since 28/05/2022
 */

@ModuleInfo(name = "module.render.particles.name", description = "module.render.particles.description", category = Category.RENDER)
public final class Particles extends Module {

    private final NumberValue multiplier = new NumberValue("Multiplier", this, 1, 1, 10, 1);
    private final BooleanValue alwaysCrit = new BooleanValue("Always Crit", this, true);

    private final BooleanValue alwaysSharpness = new BooleanValue("Always Sharpness", this, true);

    @EventLink()
    public final Listener<AttackEvent> onAttack = event -> {
        Entity target = event.getTarget();

        if (mc.thePlayer.fallDistance > 0 || alwaysCrit.getValue() || alwaysSharpness.getValue()) {
            for (int i = 0; i <= multiplier.getValue().intValue(); i++) {
                if (this.alwaysCrit.getValue()) {
                    mc.thePlayer.onCriticalHit(target);
                }

                if (this.alwaysSharpness.getValue()) {
                    this.mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.CRIT_MAGIC);
                }
            }
        }
    };
}