package cn.hackedmc.urticaria.module.impl.render.sessionstats;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.manager.TargetManager;
import cn.hackedmc.urticaria.module.impl.render.SessionStats;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PostStrafeEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.util.font.Font;
import cn.hackedmc.urticaria.util.font.FontManager;
import cn.hackedmc.urticaria.util.math.MathUtil;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;

import java.awt.*;

public class UrticariaSessionStats extends Mode<SessionStats> {
    private final BooleanValue vertical = new BooleanValue("Vertical", this, true);
    private final NumberValue alpha = new NumberValue("Alpha", this, 50, 0, 255, 1);
    private final BooleanValue blur = new BooleanValue("Blur", this, false);
    private final BooleanValue bloom = new BooleanValue("Bloom", this, false);
    private final Font icon22 = FontManager.getIconsFour(26);
    private final Font productSansMedium22 = FontManager.getProductSansBold(24);
    private final Font productSansMedium20 = FontManager.getProductSansRegular(22);
    private String speed = "";

    public UrticariaSessionStats(String name, SessionStats parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PostStrafeEvent> onPostStrafe = event -> {
        speed = MathUtil.round((MoveUtil.speed() * 20) * mc.timer.timerSpeed, 1) + "";
    };

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (mc.thePlayer == null) return;

        final float baseX = (float) getParent().position.position.x;
        final float baseY = (float) getParent().position.position.y;
        final String title = "   Session Stats   ";
        final String name = "Name: " + mc.thePlayer.getName();
        final String gamePlayer = "Game Won: " + TargetManager.wins;
        final String kd = "BPS: " + speed;
        final String kills = "Kills: " + TargetManager.kills;
        final Color color1 = getTheme().getFirstColor();
        final Color color2 = getTheme().getSecondColor();
        final float titleWidth = productSansMedium22.width(title);
        float width = Math.max(103f, Math.max(titleWidth + 27f, productSansMedium20.width(name) + 13f));
        float width2 = 13f;
        if (blur.getValue())
            NORMAL_BLUR_RUNNABLES.add(() -> {
                RenderUtil.drawIsoscelesTrapezoid(baseX + 14f, baseY + 1, titleWidth + width2, 16f, 8f, 0f, Color.WHITE);
                RenderUtil.roundedRectangleDarker(baseX, baseY + 20F, width, 60f, 0f, Color.WHITE);
            });

        if (bloom.getValue())
            NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                RenderUtil.drawRightTrapezoid(baseX, baseY, 11f, 18f, 8f, 0f, new Color(0, 0, 0, 125));
                RenderUtil.drawIsoscelesTrapezoid(baseX + 14f, baseY + 1, titleWidth + width2, 16f, 8f, 0f, new Color(0, 0, 0, 125));
                RenderUtil.drawParallelogram(baseX + titleWidth + 35F, baseY, 0.3f, 18f, -8f, 0f, new Color(0, 0, 0, 125));
                RenderUtil.roundedRectangleDarker(baseX, baseY + 20F, width, 60f, 0f, new Color(0, 0, 0, 125));
            });

        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            RenderUtil.drawGradientRightTrapezoid(baseX, baseY, 11f, 18f, 8f, 0f, color1, color2, vertical.getValue());
            RenderUtil.drawIsoscelesTrapezoid(baseX + 14f, baseY + 1, titleWidth + width2, 16f, 8f, 0f, new Color(0, 0, 0, alpha.getValue().intValue()));
            RenderUtil.drawGradientParallelogram(baseX + titleWidth + 35F, baseY, 0.3f, 18f, -8f, 0f, color1, color2, vertical.getValue());
            RenderUtil.roundedRectangleDarker(baseX, baseY + 20F, width, 60f, 0f, new Color(0, 0, 0, alpha.getValue().intValue()));

            icon22.drawString("I", baseX + 1f, baseY + 6.5f, Color.white.getRGB());
            productSansMedium22.drawString(title, baseX + 25, baseY + 6, getTheme().getAccentColor(new Vector2d(0, 100)).getRGB());
            productSansMedium20.drawString(name, baseX + 7, baseY + 27, Color.white.getRGB());

            productSansMedium20.drawString(gamePlayer, baseX + 7, baseY + productSansMedium20.height() + 30, Color.white.getRGB());

            productSansMedium20.drawString(kd, baseX + 7, baseY + productSansMedium20.height() * 2 + 33, Color.white.getRGB());

            productSansMedium20.drawString(kills, baseX + 7, baseY + productSansMedium20.height() * 3 + 36, Color.white.getRGB());
        });
        getParent().position.scale = new Vector2d(width, 80f);
    };
}
