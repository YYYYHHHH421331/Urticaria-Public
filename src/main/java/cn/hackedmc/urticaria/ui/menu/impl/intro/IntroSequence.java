package cn.hackedmc.urticaria.ui.menu.impl.intro;

import cn.hackedmc.urticaria.ui.menu.impl.main.MainMenu;
import cn.hackedmc.urticaria.util.animation.Animation;
import cn.hackedmc.urticaria.util.animation.Easing;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import util.time.StopWatch;

import java.awt.*;

/**
 * @author Hazsi
 * @since 10/12/2022
 */
public class IntroSequence extends GuiScreen {

    // ALAN TODO THIS WITH VANTAGE STUFF

    private final StopWatch timeTracker = new StopWatch();
    private boolean started = false;

    private final Animation logoAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, 3000);

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        // Init GUI is called erroneously, use this instead.
        if (!started) {
            this.started = true;

            this.timeTracker.reset();
            this.logoAnimation.setValue(255);
            this.logoAnimation.reset();
        }

        this.logoAnimation.run(0);

        ScaledResolution sr = new ScaledResolution(mc);
        RenderUtil.color(Color.WHITE);
        RenderUtil.rectangle(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), Color.BLACK);
        RenderUtil.image(new ResourceLocation("urticaria/images/splash" + mc.splashIndex + ".png"), 0,
                0, sr.getScaledWidth(), sr.getScaledHeight(), new Color(255, 255, 255, (int) this.logoAnimation.getValue()));

        if (this.timeTracker.finished(4000)) {
//            mc.displayGuiScreen(new LoginMenu());
//            mc.displayGuiScreen(new GuiMainMenu());
            mc.displayGuiScreen(new MainMenu());
        }
    }
}