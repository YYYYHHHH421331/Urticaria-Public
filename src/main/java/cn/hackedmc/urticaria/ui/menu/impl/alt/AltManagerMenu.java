package cn.hackedmc.urticaria.ui.menu.impl.alt;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.ui.menu.impl.alt.impl.AltDisplay;
import cn.hackedmc.urticaria.util.MouseUtil;
import cn.hackedmc.urticaria.util.RandomUtil;
import cn.hackedmc.urticaria.util.animation.Animation;
import cn.hackedmc.urticaria.util.animation.Easing;
import cn.hackedmc.urticaria.util.gui.ScrollUtil;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.util.render.ScissorUtil;
import cn.hackedmc.urticaria.util.shader.RiseShaders;
import cn.hackedmc.urticaria.util.shader.base.ShaderRenderType;
import cn.hackedmc.urticaria.ui.menu.Menu;
import cn.hackedmc.urticaria.ui.menu.component.button.MenuButton;
import cn.hackedmc.urticaria.ui.menu.component.button.impl.MenuFeedBackTextButton;
import cn.hackedmc.urticaria.util.account.Account;
import cn.hackedmc.urticaria.util.account.microsoft.MicrosoftLogin;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.lwjgl.opengl.GL11;
import util.time.StopWatch;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class AltManagerMenu extends Menu {

    private static final double ACCOUNT_WIDTH = 180;
    private static final double ACCOUNT_HEIGHT = 32;
    private static final int ACCOUNT_SPACING = 6;

    private static final int BOX_SPACING = 10;
    private static final int BUTTON_WIDTH = 180;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_SPACING = 6;

    public final List<AltDisplay> altDisplays = new ArrayList<>();
    private final ScrollUtil scrollUtil = new ScrollUtil();

    private MenuFeedBackTextButton loginThroughBrowserButton, loginNeteaseAltButton, loginOfflineAltButton;
    private MenuButton[] menuButtons;

    private Animation animation = new Animation(Easing.EASE_OUT_QUINT, 500);

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        // Renders the background
//        RiseShaders.MAIN_MENU_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, null);
        RenderUtil.image(new ResourceLocation("urticaria/images/mainmenu.png"), 0, 0, width, height);

        // Update animations
        animation.run(this.height);

        // Handles scrolling
        int visibleHeight = this.height - 20 - BUTTON_HEIGHT - BUTTON_SPACING * 2;
        int listHeight = (int) (Math.ceil(altDisplays.size() / 2f) * (32 + ACCOUNT_SPACING));
        scrollUtil.setMax(-listHeight + visibleHeight - (BOX_SPACING - ACCOUNT_SPACING));
        scrollUtil.onRender();

        // Renders the buttons
        this.loginThroughBrowserButton.draw(mouseX, mouseY, partialTicks);
        this.loginNeteaseAltButton.draw(mouseX, mouseY, partialTicks);
        this.loginOfflineAltButton.draw(mouseX, mouseY, partialTicks);

        // Blur and bloom box
        double boxX = this.width / 2 - ACCOUNT_WIDTH - ACCOUNT_SPACING / 2 - BOX_SPACING;
        double boxY = (altDisplays.isEmpty() ? 20 : altDisplays.get(0).getY()) + this.height - animation.getValue() - BOX_SPACING + scrollUtil.getScroll();
        double boxWidth = ACCOUNT_WIDTH * 2 + ACCOUNT_SPACING + BOX_SPACING * 2;
        double boxHeight = (altDisplays.isEmpty() ? 0 /* add an alt*/ : (ACCOUNT_HEIGHT + ACCOUNT_SPACING) * altDisplays.size() - ACCOUNT_SPACING + BOX_SPACING * 2);
        double finalBoxHeight = Math.min(this.height - BUTTON_HEIGHT - BUTTON_SPACING * 2 - 10 - this.scrollUtil.scroll, boxHeight);

        // Rendering the background box
        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle(boxX, boxY, boxWidth, finalBoxHeight, 10, Color.WHITE));
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle(boxX, boxY, boxWidth, finalBoxHeight, 10, new Color(0, 0, 0, 100)));

        double offsetY = this.height - this.animation.getValue() + scrollUtil.getScroll();
        for (AltDisplay altDisplay : altDisplays) {
            altDisplay.draw(offsetY, mouseX, mouseY);
        }

        ScaledResolution scaledResolution = new ScaledResolution(mc);

        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.rectangle(0, 0, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), Color.BLACK));

        // Run blur
//        RiseShaders.GAUSSIAN_BLUR_SHADER.update();
//        RiseShaders.GAUSSIAN_BLUR_SHADER.run(ShaderRenderType.OVERLAY, mc.timer.renderPartialTicks, InstanceAccess.NORMAL_BLUR_RUNNABLES);

        // Run bloom
        RiseShaders.POST_BLOOM_SHADER.update();
        RiseShaders.POST_BLOOM_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, InstanceAccess.NORMAL_POST_BLOOM_RUNNABLES);

        // Run post shader things
        NORMAL_BLUR_RUNNABLES.clear();
        NORMAL_POST_BLOOM_RUNNABLES.clear();

        // Remove the area where no accounts should be displayed
        GL11.glPushMatrix();
        ScissorUtil.enable();
        NORMAL_RENDER_RUNNABLES.forEach(Runnable::run);
        NORMAL_RENDER_RUNNABLES.clear();
        ScissorUtil.scissor(new ScaledResolution(mc), 0, 0, this.width, this.height - BUTTON_HEIGHT - BUTTON_SPACING - ACCOUNT_SPACING);
        ScissorUtil.disable();
        GL11.glPopMatrix();

        UI_BLOOM_RUNNABLES.forEach(Runnable::run);
        UI_BLOOM_RUNNABLES.clear();
        // TODO: Don't forget to NOT render the displays out of the screen to save performance
    }

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        if (this.menuButtons == null) return;

        // If doing a left click and the mouse is hovered over a button, execute the buttons action (runnable)
        if (mouseButton == 0) {
            for (MenuButton menuButton : this.menuButtons) {
                if (MouseUtil.isHovered(menuButton.getX(), menuButton.getY(), menuButton.getWidth(), menuButton.getHeight(), mouseX, mouseY)) {
                    menuButton.runAction();
                    break;
                }
            }

            boolean resetAltDisplays = false;

            for (int i = 0; i < altDisplays.size(); i++) {
                AltDisplay altDisplay = altDisplays.get(i);
                if (MouseUtil.isHovered(altDisplay.getX(), altDisplay.getY() + scrollUtil.getScroll(), altDisplay.getWidth(), altDisplay.getHeight(), mouseX, mouseY)) {

                    double deleteX = altDisplay.getX() + altDisplay.getWidth() - 16;
                    double deleteY = altDisplay.getY() + 4;
                    if (MouseUtil.isHovered(deleteX, deleteY + scrollUtil.scroll, 12, 12, mouseX, mouseY)) {
                        Account account = altDisplay.getAccount();
                        System.out.println("Deleting account: " + account.getUsername()); // 添加打印语句
                        Client.INSTANCE.getAltManager().getAccounts().remove(account);
                        this.altDisplays.remove(altDisplay);
                        resetAltDisplays = true;
                        break;
                    }

                    Client.INSTANCE.getAltManager().isNetease = false;

                    Account account = altDisplay.getAccount();
                    String refreshToken = account.getRefreshToken();
                    switch (account.getType()) {
                        case "Offline": {
                            mc.session = new Session(account.getUsername(), account.getUuid(), account.getRefreshToken(), "mojang");
                            for (AltDisplay d : altDisplays) if (d.isSelected()) d.setSelected(false);
                            altDisplay.setSelected(true);

                            break;
                        }

                        case "Microsoft": {
                            if (refreshToken != null) {
                                new Thread(() -> {
                                    MicrosoftLogin.LoginData loginData = loginWithRefreshToken(refreshToken);
                                    if (loginData != null) {
                                        account.setUsername(loginData.username);
                                        account.setRefreshToken(loginData.newRefreshToken);
                                        this.unselectDisplays();
                                        altDisplay.setSelected(true);
                                    } else {
                                        altDisplay.setSelected(false);
                                        altDisplay.setDisplayColor(Color.RED); // 登录失败，设置颜色为红色
                                    }
                                }).start();
                            }

                            break;
                        }

                        case "Netease": {

                            break;
                        }
                    }
                    break;
                }
            }

            if (resetAltDisplays) {
                System.out.println("Reloading Alt Displays...");
                Client.INSTANCE.getAltManager().set("alts");
                this.loadAltDisplays();
            }
        }
    }


    @Override
    public void initGui() {
        int centerX = this.width / 2;
        int buttonX = centerX - BUTTON_WIDTH - 10;
        int buttonX2 = centerX + 10;
        int buttonY = this.height - BUTTON_HEIGHT - BUTTON_SPACING;
        scrollUtil.reset();

        // Re-creates the buttons for not having to care about the animation reset
        this.loginThroughBrowserButton = new MenuFeedBackTextButton(buttonX, buttonY - BUTTON_HEIGHT - BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT, () -> {
            MicrosoftLogin.getRefreshToken(refreshToken -> {
                if (refreshToken != null) {
                    new Thread(() -> {
                        MicrosoftLogin.LoginData loginData = loginWithRefreshToken(refreshToken);
                        if (loginData != null) {
                            Account account = new Account(loginData.username, "************", loginData.username, loginData.uuid, loginData.newRefreshToken);
                            Client.INSTANCE.getAltManager().getAccounts().add(account);

                            AltDisplay display = getAltDisplay(centerX, account);
                            this.unselectDisplays();
                            this.altDisplays.add(display);
                            Client.INSTANCE.getAltManager().set("alts");
                        } else {
                            System.out.println("Data null.");
                        }
                    }).start();
                }
            });
        }, "Login through browser", "Copied to your clipboard");

        this.loginNeteaseAltButton = new MenuFeedBackTextButton(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, () -> {
            String cookie = getClipboardString();
            if (cookie.startsWith("{")) cookie = cookie.substring(1);
            if (cookie.endsWith("}")) cookie = cookie.substring(0, cookie.length() - 1);
            Account account = new Account(cookie);
            Client.INSTANCE.getAltManager().getAccounts().add(account);

            this.unselectDisplays();
            AltDisplay display = getAltDisplay(centerX, account);
            this.altDisplays.add(display);
            Client.INSTANCE.getAltManager().set("alts");
        }, "Login Netease Alt", "Logan");

        this.loginOfflineAltButton = new MenuFeedBackTextButton(buttonX2, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, () -> {
            final String name = getClipboardString().equals("") || getClipboardString().length() > 30 ? RandomUtil.randomName() : getClipboardString();
            Account account = new Account(name, "Offline");
            account.setOfflineUsername(name);
            account.setRefreshToken("");
            mc.session = new Session(account.getUsername(), account.getUuid(), account.getRefreshToken(), "mojang");
            Client.INSTANCE.getAltManager().getAccounts().add(account);

            this.unselectDisplays();
            AltDisplay display = getAltDisplay(centerX, account);
            this.altDisplays.add(display);
            Client.INSTANCE.getAltManager().set("alts");
        }, "Login Offline Alt", "Enter your name");

        // Re-create the logo animation for not having to care about its reset
        this.animation = new Animation(Easing.EASE_OUT_QUINT, 500);

        // Putting all buttons in an array for handling mouse clicks
        this.menuButtons = new MenuButton[]{this.loginThroughBrowserButton, this.loginNeteaseAltButton, this.loginOfflineAltButton};

        // TODO: Load saved account information from file
        // TODO: Maybe run skin-gathering on external thread

        this.loadAltDisplays();
    }

    private AltDisplay getAltDisplay(int centerX, Account account) {
        AltDisplay display;
        if (!altDisplays.isEmpty()) {
            AltDisplay prevDisplay = altDisplays.get(altDisplays.size() - 1);
            boolean newRow = altDisplays.size() % 2 == 0;
            display = new AltDisplay(centerX + (newRow ? -ACCOUNT_WIDTH - ACCOUNT_SPACING / 2.0 : ACCOUNT_SPACING / 2.0), (newRow ? prevDisplay.getY() + prevDisplay.getHeight() + ACCOUNT_SPACING : prevDisplay.getY()), ACCOUNT_WIDTH, ACCOUNT_HEIGHT, account);
        } else {
            display = new AltDisplay(centerX - ACCOUNT_SPACING / 2.0 - ACCOUNT_WIDTH, 20, ACCOUNT_WIDTH, ACCOUNT_HEIGHT, account);
        }

        display.setSelected(true);
        return display;
    }

    private MicrosoftLogin.LoginData loginWithRefreshToken(String refreshToken) {
        final MicrosoftLogin.LoginData loginData = MicrosoftLogin.login(refreshToken);
        if (loginData == null) return null;
        mc.session = new Session(loginData.username, loginData.uuid, loginData.mcToken, "microsoft");
        return loginData;
    }

    private void loadAltDisplays() {
        double centerX = width / 2.0;
        double accountY = 20;
        this.altDisplays.clear();
        List<Account> accounts = Client.INSTANCE.getAltManager().getAccounts();
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            AltDisplay display = new AltDisplay(centerX + (i % 2 == 0 ? -ACCOUNT_WIDTH - ACCOUNT_SPACING / 2.0 : ACCOUNT_SPACING / 2.0), accountY, ACCOUNT_WIDTH, ACCOUNT_HEIGHT, account);
            if (mc.session != null && account != null && account.getUsername() != null && mc.session.getUsername() != null &&
                    account.getUsername().equals(mc.session.getUsername())) display.setSelected(true);
            altDisplays.add(display);
            if (i % 2 == 1) accountY += ACCOUNT_HEIGHT + ACCOUNT_SPACING;
        }
    }

    private void unselectDisplays() {
        for (AltDisplay d : altDisplays) if (d.isSelected()) d.setSelected(false);
    }

    public AltManagerMenu() {
        Client.INSTANCE.getAltManager().get("alts").read();
    }
}
