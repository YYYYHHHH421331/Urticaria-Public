package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.render.ProjectionComponent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.render.targetinfo.ModernTargetInfo;
import cn.hackedmc.urticaria.module.impl.render.targetinfo.ShineTargetInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.AttackEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.DragValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.Entity;
import util.time.StopWatch;

import javax.vecmath.Vector4d;

/**
 * @author Alan
 * @since 10/19/2021
 */

@Rise
@ModuleInfo(name = "module.render.targetinfo.name", description = "module.render.targetinfo.description", category = Category.RENDER)
public final class TargetInfo extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new ModernTargetInfo("Modern", this))
            .add(new ShineTargetInfo("Shine", this))
            .setDefault("Shine");
    public final DragValue positionValue = new DragValue("Position", this, new Vector2d(200, 200));
    public final BooleanValue followPlayer = new BooleanValue("Follow Player", this, false);

    public Vector2d position = new Vector2d(0, 0);
    public Entity target;
    public double distanceSq;
    public boolean inWorld;
    public StopWatch stopwatch = new StopWatch();

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

//        target = mc.thePlayer;

        if (mc.currentScreen instanceof GuiChat) {
            stopwatch.reset();
            target = mc.thePlayer;
        }

        if (target == null) {
            inWorld = false;
            return;
        }

        distanceSq = mc.thePlayer.getDistanceSqToEntity(target);
        inWorld = mc.theWorld.loadedEntityList.contains(target);
    };

    @EventLink()
    public final Listener<AttackEvent> onAttack = event -> {

        if (event.getTarget() instanceof AbstractClientPlayer) {
            target = event.getTarget();
            stopwatch.reset();
        }
    };


    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {

        if (target == null) {
            return;
        }

        if (this.followPlayer.getValue()) {
            Vector4d position = ProjectionComponent.get(target);

            if (position == null) return;

            this.position.x = position.z;
            this.position.y = position.w - (position.w - position.y) / 2 - this.positionValue.scale.y / 2f;
        } else {
            this.position = positionValue.position;
        }
    };
}