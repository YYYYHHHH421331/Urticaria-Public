package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.impl.motion.PostStrafeEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.util.math.MathUtil;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.util.vector.Vector2f;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.DragValue;

import java.awt.*;

@ModuleInfo(name = "module.render.xpcounter.name", description = "module.render.xpcounter.description", category = Category.RENDER)
public final class XPCounter extends Module {
    private final BooleanValue showTitle = new BooleanValue("Title", this, false);
    private final DragValue position = new DragValue("Position", this, new Vector2d(200, 200));

    private final Vector2f scale = new Vector2f(RenderUtil.GENERIC_SCALE, RenderUtil.GENERIC_SCALE);

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {

        Vector2d position = this.position.position;

        final String titleString = showTitle.getValue() ? "XP " : "";
        final String xpString = String.valueOf(mc.thePlayer.experienceLevel);

        final float titleWidth = nunitoNormal.width(titleString);
        scale.x = titleWidth + nunitoNormal.width(xpString);

        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            RenderUtil.roundedRectangle(position.x, position.y, scale.x + 6, scale.y - 1, getTheme().getRound(), getTheme().getBackgroundShade());

            this.position.setScale(new Vector2d(scale.x + 6, scale.y - 1));

            final double textX = position.x + 3.0F;
            final double textY = position.y + scale.y / 2.0F - nunitoNormal.height() / 4.0F;
            nunitoNormal.drawStringWithShadow(titleString, textX, textY, getTheme().getFirstColor().getRGB());
            nunitoNormal.drawStringWithShadow(xpString, textX + titleWidth, textY, Color.WHITE.getRGB());
        });

        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle(position.x, position.y, scale.x + 6, scale.y - 1, getTheme().getRound(), Color.BLACK));
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle(position.x, position.y, scale.x + 6, scale.y - 1, getTheme().getRound() + 1, getTheme().getDropShadow()));
    };
}