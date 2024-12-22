package cn.hackedmc.urticaria.ui.menu.impl.main;

import cn.hackedmc.urticaria.ui.menu.component.button.impl.MainMenuButton;
import cn.hackedmc.urticaria.util.MouseUtil;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.ui.menu.Menu;
import cn.hackedmc.urticaria.ui.menu.component.button.MenuButton;
import cn.hackedmc.urticaria.util.animation.Animation;
import cn.hackedmc.urticaria.util.animation.Easing;
import cn.hackedmc.urticaria.util.font.Font;
import cn.hackedmc.urticaria.util.font.FontManager;
import cn.hackedmc.fucker.Fucker;
import cn.hackedmc.urticaria.util.shader.RiseShaders;
import cn.hackedmc.urticaria.util.shader.base.ShaderRenderType;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.Display;
import util.time.StopWatch;

import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class MainMenu extends Menu {
    private Font TITLE_FONT = FontManager.getBiko(45);

    private Animation rect1;
    private Animation rect2;
    private Animation rect3;
    private Animation button;
    private Animation logoYAnimation;
    private Animation logoXAnimation;

    private float logoY;
    private float logoX;

    private MainMenuButton[] menuButtons;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
//        RiseShaders.MAIN_MENU_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, null);
        RenderUtil.image(new ResourceLocation("urticaria/images/mainmenu.png"), 0, 0, width, height);
        this.rect1.run(width / 2.5);
        this.rect2.run(width / 2.5 + width / 8f);
        this.rect3.run(width / 2.5 + width / 5f);
        this.button.run(width / 11f + 100);
        this.logoYAnimation.run(this.logoY + width / 10f);
        this.logoXAnimation.run(this.logoX + width / 10f);
        NORMAL_BLUR_RUNNABLES.add(() -> {
            RenderUtil.drawRightTrapezoid((float) (-width / 2.5f + this.rect1.getValue()), 0, width / 2.5f, height, height / -3f, 0f, Color.WHITE);
            RenderUtil.drawRightTrapezoid((float) (-width / 2.5f - width / 8f + this.rect2.getValue()), 0, width / 2.5f + width / 8f, height, height / -3f, 0f, Color.WHITE);
            RenderUtil.drawRightTrapezoid((float) (-width / 2.5f - width / 5f + this.rect3.getValue()), 0, width / 2.5f + width / 5f, height, height / -3f, 0f, Color.WHITE);
        });
        RiseShaders.GAUSSIAN_BLUR_SHADER.update();
        RiseShaders.GAUSSIAN_BLUR_SHADER.run(ShaderRenderType.OVERLAY, mc.timer.renderPartialTicks, InstanceAccess.NORMAL_BLUR_RUNNABLES);
        NORMAL_BLUR_RUNNABLES.clear();
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
            RenderUtil.drawRightTrapezoid((float) (-width / 2.5f + this.rect1.getValue()), 0, width / 2.5f, height, height / -3f, 0f, Color.BLACK);
            RenderUtil.drawRightTrapezoid((float) (-width / 2.5f - width / 8f + this.rect2.getValue()), 0, width / 2.5f + width / 8f, height, height / -3f, 0f, Color.BLACK);
            RenderUtil.drawRightTrapezoid((float) (-width / 2.5f - width / 5f + this.rect3.getValue()), 0, width / 2.5f + width / 5f, height, height / -3f, 0f, Color.BLACK);
        });
        RiseShaders.POST_BLOOM_SHADER.update();
        RiseShaders.POST_BLOOM_SHADER.run(ShaderRenderType.OVERLAY, mc.timer.renderPartialTicks, InstanceAccess.NORMAL_POST_BLOOM_RUNNABLES);
        NORMAL_POST_BLOOM_RUNNABLES.clear();

        RenderUtil.drawRightTrapezoid((float) (-width / 2.5f + this.rect1.getValue()), 0, width / 2.5f, height, height / -3f, 0f, new Color(0, 0, 0, 150));
        RenderUtil.drawRightTrapezoid((float) (-width / 2.5f - width / 8f + this.rect2.getValue()), 0, width / 2.5f + width / 8f, height, height / -3f, 0f, new Color(0, 0, 0, 100));
        RenderUtil.drawRightTrapezoid((float) (-width / 2.5f - width / 5f + this.rect3.getValue()), 0, width / 2.5f + width / 5f, height, height / -3f, 0f, new Color(0, 0, 0, 50));
        for (MainMenuButton menuButton : this.menuButtons) {
            menuButton.setX((float) (this.button.getValue() - 100));
            menuButton.draw(mouseX, mouseY, partialTicks);
        }
        RenderUtil.image(new ResourceLocation("urticaria/icons/main_menu/logo.png"), this.logoXAnimation.getValue() - width / 10f, this.logoYAnimation.getValue() - width / 10f, width / 10f, width / 10f);
        TITLE_FONT.drawString(Client.NAME, this.logoXAnimation.getValue(), this.logoYAnimation.getValue() - width / 10f + width / 30f, new Color(233, 233, 233, 200).getRGB());
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.menuButtons == null) return;
        // If doing a left click and the mouse is hovered over a button, execute the buttons action (runnable)
        if (mouseButton == 0) {
            for (MenuButton menuButton : this.menuButtons) {
                if (MouseUtil.isHovered(menuButton.getX(), menuButton.getY(), menuButton.getWidth(), menuButton.getHeight(), mouseX, mouseY)) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    menuButton.runAction();
                    break;
                }
            }
        }
    }

    @Override
    public void initGui() {
        long totalMilliSeconds = System.currentTimeMillis();
        Date nowTime = new Date(totalMilliSeconds);
        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd");
        String retStrFormatNowDate = sdf3.format(nowTime);
        //Reset
        InstanceAccess.clearRunnables();
        int centerY = height / 2;
        int buttonHeight = (int) MainMenuButton.FONT_RENDERER.height();
        int buttonSpacing = 6;
        int buttonX = -100;
        int buttonY = centerY - buttonHeight * 2 - buttonSpacing * 2;
        MainMenuButton singlePlayerButton = new MainMenuButton(buttonX, buttonY, () -> mc.displayGuiScreen(new GuiSelectWorld(this)), "Singleplayer", new ResourceLocation("urticaria/icons/main_menu/s.png"));
        MainMenuButton multiPlayerButton = new MainMenuButton(buttonX, buttonY + buttonHeight + buttonSpacing, () -> mc.displayGuiScreen(new GuiMultiplayer(this)), "Multiplayer", new ResourceLocation("urticaria/icons/main_menu/m.png"));
        MainMenuButton altManagerButton = new MainMenuButton(buttonX, buttonY + buttonHeight * 2 + buttonSpacing * 2, () -> mc.displayGuiScreen(Client.INSTANCE.getAltManagerMenu()), "Alts", Client.INSTANCE.getAltManager().getAccounts().isEmpty() ? new ResourceLocation("urticaria/icons/main_menu/a_2.png") : new ResourceLocation("urticaria/icons/main_menu/a_1.png"));
        MainMenuButton settingButton = new MainMenuButton(buttonX, buttonY + buttonHeight * 3 + buttonSpacing * 3, () -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)), "Settings", new ResourceLocation("urticaria/icons/main_menu/tool.png"));
        MainMenuButton exitButton = new MainMenuButton(buttonX, buttonY + buttonHeight * 4 + buttonSpacing * 4, mc::shutdown, "Exit", new ResourceLocation("urticaria/icons/main_menu/e.png"));
        this.menuButtons = new MainMenuButton[]{singlePlayerButton, multiPlayerButton, altManagerButton, settingButton, exitButton};
        this.rect1 = new Animation(Easing.EASE_OUT_QUINT, 1000);
        this.rect2 = new Animation(Easing.EASE_OUT_QUINT, 1200);
        this.rect3 = new Animation(Easing.EASE_OUT_QUINT, 1400);
        this.button = new Animation(Easing.EASE_OUT_QUINT, 1300);
        this.logoYAnimation = new Animation(Easing.EASE_OUT_QUINT, 1500);
        this.logoXAnimation = new Animation(Easing.EASE_OUT_QUINT, 1500);
        this.logoX = width / 30f;
        this.logoY = buttonY / 2f - width / 20f;
        this.TITLE_FONT = FontManager.getBiko(width / 10);
        Display.setTitle(Client.NAME + " " + Client.VERSION + " | " + "User:IDEA READY | " + retStrFormatNowDate);
    }
}
