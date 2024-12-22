package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.player.BlinkComponent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.other.WorldChangeEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.util.font.Font;
import cn.hackedmc.urticaria.util.font.FontManager;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.math.MathUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.BoundsNumberValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import net.minecraft.client.entity.EntityOtherPlayerMP;

import java.awt.*;

/**
 * @author Alan
 * @since 23/10/2021
 */

@Rise
@ModuleInfo(name = "module.player.blink.name", description = "module.player.blink.description", category = Category.PLAYER)
public class Blink extends Module {
    private final Font NORMAL20 = FontManager.getProductSansRegular(24);

    private final BooleanValue info = new BooleanValue("Blink Info", this, false);
    private final NumberValue maxTime = new NumberValue("Max Blink Time", this, 29f, 10f, 60f, 0.1f, () -> !info.getValue());
    private final BooleanValue blur = new BooleanValue("Blur", this, true, () -> !info.getValue());
    private final BooleanValue bloom = new BooleanValue("Bloom", this, true, () -> !info.getValue());
    public final BooleanValue pulse = new BooleanValue("Pulse", this, false);
    public final BooleanValue incoming = new BooleanValue("Incoming", this, false);
    public final BoundsNumberValue delay = new BoundsNumberValue("Delay", this, 2, 2, 2, 40, 1, () -> !pulse.getValue());
    public int next;
    private long startTime;

    @Override
    protected void onEnable() {
        getNext();
        startTime = System.currentTimeMillis();
        BlinkComponent.blinking = true;
    }

    @Override
    protected void onDisable() {
        BlinkComponent.blinking = false;
    }

    @EventLink()
    public final Listener<WorldChangeEvent> onWorldChange = event -> getNext();

    @EventLink
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (!info.getValue()) return;

        final float step = Math.min(maxTime.getValue().floatValue(), (System.currentTimeMillis() - startTime) / 1000f);

        final int midWidth = event.getScaledResolution().getScaledWidth() / 2;
        final int x = midWidth - 75;
        final int y = event.getScaledResolution().getScaledHeight() / 2 - 40;

        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            RenderUtil.roundedRectangle(x, y, 150, 40, 3f, new Color(0, 0, 0, 60));
            NORMAL20.drawCenteredString("Blink Time", midWidth, y + 10, new Color(233, 233, 233, 233).getRGB());
            RenderUtil.roundedRectangle(x + 5, y + 24, 140, 6, 3f, new Color(0, 0, 0, 100));
            RenderUtil.drawRoundedGradientRect(x + 5, y + 24, 140f / maxTime.getValue().floatValue() * step, 6f, 3f, getTheme().getFirstColor(), getTheme().getSecondColor(), false);
        });

        if (blur.getValue()) {
            NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangleDarker(x, y, 150, 40, 3f, Color.WHITE));
        }

        if (bloom.getValue()) {
            NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle(x, y, 150, 40, 3f, Color.BLACK));
        }
    };

    public void getNext() {
        if (InstanceAccess.mc.thePlayer == null) return;
        next = InstanceAccess.mc.thePlayer.ticksExisted + (int) MathUtil.getRandom(delay.getValue().intValue(), delay.getSecondValue().intValue());
    }
}