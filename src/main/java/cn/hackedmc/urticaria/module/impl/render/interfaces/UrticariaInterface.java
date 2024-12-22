package cn.hackedmc.urticaria.module.impl.render.interfaces;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.Type;
import cn.hackedmc.urticaria.module.impl.render.Interface;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.other.TickEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.util.font.Font;
import cn.hackedmc.urticaria.util.font.FontManager;
import cn.hackedmc.urticaria.util.player.ServerUtil;
import cn.hackedmc.urticaria.util.render.ColorUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.util.shader.base.RiseShaderProgram;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class UrticariaInterface extends Mode<Interface> {
    private final Font italian = FontManager.getItalian(18);
    private final Font bike = FontManager.getBiko(18);
    private final Font productSansRegular = FontManager.getProductSansRegular(18);
    private final Font minecraft = FontManager.getMinecraft();

    private Font arrayListFont = productSansRegular;

    private final Font icon22 = FontManager.getIconsFour(26);
    private final Font productSansMedium22 = FontManager.getProductSansBold(24);
    private final Font productSansMedium20 = FontManager.getProductSansMedium(22);

    public UrticariaInterface(String name, Interface parent) {
        super(name, parent);
    }

    private final ModeValue colorMode = new ModeValue("ArrayList Color Mode", this, () -> Client.CLIENT_TYPE != Type.BASIC) {{
        add(new SubMode("Static"));
        add(new SubMode("Fade"));
        add(new SubMode("Breathe"));
        setDefault("Fade");
    }};

    private final ModeValue font = new ModeValue("ArrayList Font", this, () -> Client.CLIENT_TYPE != Type.BASIC) {{
        add(new SubMode("Product Sans"));
        add(new SubMode("Bike"));
        add(new SubMode("Italian"));
        add(new SubMode("Minecraft"));
        setDefault("Product Sans");
    }};

    private final ModeValue shader = new ModeValue("Shader Effect", this, () -> Client.CLIENT_TYPE != Type.BASIC) {{
        add(new SubMode("Glow"));
        add(new SubMode("Shadow"));
        add(new SubMode("None"));
        setDefault("Shadow");
    }};

    private final BooleanValue dropShadow = new BooleanValue("Drop Shadow", this, true, () -> Client.CLIENT_TYPE != Type.BASIC);
    private final BooleanValue sidebar = new BooleanValue("Sidebar", this, true, () -> Client.CLIENT_TYPE != Type.BASIC);
    private final ModeValue background = new ModeValue("BackGround", this, () -> Client.CLIENT_TYPE != Type.BASIC) {{
        add(new SubMode("Off"));
        add(new SubMode("Normal"));
        add(new SubMode("Blur"));
        setDefault("Normal");
    }};
    private boolean glow, shadow;
    private boolean normalBackGround, blurBackGround;

    private final BooleanValue logo = new BooleanValue("Logo", this, true, () -> Client.CLIENT_TYPE != Type.BASIC);
    private final BooleanValue logoVertical = new BooleanValue("Vertical", this, true, logo::getValue);
    private final NumberValue logoAlpha = new NumberValue("Alpha", this, 50, 0, 255, 1, logo::getValue);
    private final BooleanValue blur = new BooleanValue("Blur", this, false, logo::getValue);
    private final BooleanValue bloom = new BooleanValue("Bloom", this, false, logo::getValue);

    @EventLink
    public final Listener<TickEvent> onTick = event -> {
        glow = this.shader.getValue().getName().equals("Glow");
        shadow = this.shader.getValue().getName().equals("Shadow");
        arrayListFont = this.font.getValue().getName().equals("Minecraft") ? minecraft : (this.font.getValue().getName().equals("Bike") ? bike : (this.font.getValue().getName().equals("Italian") ? italian : productSansRegular));

        normalBackGround = background.getValue().getName().equals("Normal");
        blurBackGround = normalBackGround || background.getValue().getName().equals("Blur");

        // modules in the top right corner of the screen
        for (final ModuleComponent moduleComponent : this.getParent().getActiveModuleComponents()) {
            if (moduleComponent.animationTime == 0) {
                continue;
            }

            final boolean hasTag = !moduleComponent.getTag().isEmpty() && this.getParent().suffix.getValue();

            String name = (this.getParent().lowercase.getValue() ? moduleComponent.getTranslatedName().toLowerCase() : moduleComponent.getTranslatedName()).replace(getParent().getRemoveSpaces().getValue() ? " " : "", "");

            String tag = (this.getParent().lowercase.getValue() ? moduleComponent.getTag().toLowerCase() : moduleComponent.getTag())
                    .replace(getParent().getRemoveSpaces().getValue() ? " " : "", "");

            Color color = this.getTheme().getFirstColor();
            switch (this.colorMode.getValue().getName()) {
                case "Breathe": {
                    double factor = this.getTheme().getBlendFactor(new Vector2d(0, 0));
                    color = ColorUtil.mixColors(this.getTheme().getFirstColor(), this.getTheme().getSecondColor(), factor);
                    break;
                }
                case "Fade": {
                    color = this.getTheme().getAccentColor(new Vector2d(0, moduleComponent.getPosition().getY()));
                    break;
                }
            }

            moduleComponent.setColor(color);

            moduleComponent.setNameWidth(arrayListFont.width(name));
            moduleComponent.setTagWidth(hasTag ? (arrayListFont.width(tag) + 4) : 0);
        }
    };

    @EventLink
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        this.getParent().setModuleSpacing(this.productSansRegular.height());
        this.getParent().setWidthComparator(this.arrayListFont);
        this.getParent().setEdgeOffset(10);

        for (final ModuleComponent moduleComponent : this.getParent().getActiveModuleComponents()) {
            if (moduleComponent.animationTime == 0) {
                continue;
            }

            String name = (this.getParent().lowercase.getValue() ? moduleComponent.getTranslatedName().toLowerCase() : moduleComponent.getTranslatedName()).replace(getParent().getRemoveSpaces().getValue() ? " " : "", "");

            String tag = (this.getParent().lowercase.getValue() ? moduleComponent.getTag().toLowerCase() : moduleComponent.getTag())
                    .replace(getParent().getRemoveSpaces().getValue() ? " " : "", "");

            final double x = moduleComponent.getPosition().getX() + 4.0F;
            final double y = moduleComponent.getPosition().getY() - 2.0F;
            final Color finalColor = moduleComponent.getColor();

            final double widthOffset = arrayListFont == minecraft ? 3.5 : 2;

            if (this.normalBackGround || this.blurBackGround) {
                Runnable backgroundRunnable = () ->
                        RenderUtil.rectangle(x + 0.5 - widthOffset, y - 2.5,
                                (moduleComponent.nameWidth + moduleComponent.tagWidth) + 2 + widthOffset,
                                this.getParent().moduleSpacing, getTheme().getBackgroundShade());
                if (this.normalBackGround) {
                    NORMAL_RENDER_RUNNABLES.add(backgroundRunnable);
                }

                if (this.blurBackGround) {
                    NORMAL_BLUR_RUNNABLES.add(() ->
                            RenderUtil.rectangle(x + 0.5 - widthOffset, y - 2.5,
                                    (moduleComponent.nameWidth + moduleComponent.tagWidth) + 2 + widthOffset,
                                    this.getParent().moduleSpacing, Color.BLACK));
                }

                // Draw the glow/shadow around the module
                NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                    if (glow || shadow) {
                        RenderUtil.rectangle(x + 0.5 - widthOffset, y - 2.5,
                                (moduleComponent.nameWidth + moduleComponent.tagWidth) + 2 + widthOffset,
                                this.getParent().moduleSpacing, glow ? ColorUtil.withAlpha(finalColor, 164) : getTheme().getDropShadow());
                    }
                });
            }

            final boolean hasTag = !moduleComponent.getTag().isEmpty() && this.getParent().suffix.getValue();

            Runnable textRunnable = () -> {
                if (dropShadow.getValue()) {
                    arrayListFont.drawStringWithShadow(name, x, y, finalColor.getRGB());

                    if (hasTag) {
                        arrayListFont.drawStringWithShadow(tag, x + moduleComponent.getNameWidth() + 3, y, 0xFFCCCCCC);
                    }
                } else {
                    arrayListFont.drawString(name, x, y, finalColor.getRGB());

                    if (hasTag) {
                        arrayListFont.drawString(tag, x + moduleComponent.getNameWidth() + 3, y, 0xFFCCCCCC);
                    }
                }
            };

            Runnable shadowRunnable = () -> {
                arrayListFont.drawString(name, x, y, Color.BLACK.getRGB());

                if (hasTag) {
                    arrayListFont.drawString(tag, x + moduleComponent.getNameWidth() + 3, y, Color.BLACK.getRGB());
                }
            };

            NORMAL_RENDER_RUNNABLES.add(textRunnable);

            if (glow) {
                NORMAL_POST_BLOOM_RUNNABLES.add(textRunnable);
            } else if (shadow) {
                NORMAL_POST_BLOOM_RUNNABLES.add(shadowRunnable);
            }

            if (this.sidebar.getValue()) {
                Runnable runnable = () -> RenderUtil.roundedRectangle(x + moduleComponent.getNameWidth() + moduleComponent.getTagWidth() + 2, y - 1.5f,
                        2, 9, 1, finalColor);

                runnable.run();
                NORMAL_POST_BLOOM_RUNNABLES.add(runnable);
            }
        }

        final float logoX = (float) getParent().logoDrag.position.x;
        final float logoY = (float) getParent().logoDrag.position.y;
        final Color color1 = getTheme().getFirstColor();
        final Color color2 = getTheme().getSecondColor();
        final String displayText = mc.thePlayer.getCommandSenderName() + " | " + Minecraft.getDebugFPS() + "FPS | " + ServerUtil.getRemoteIp();
        final int length = productSansMedium20.width(displayText);
        final int clientNameLength = productSansMedium22.width(Client.NAME);

        if (logo.getValue()) {
            if (blur.getValue())
                NORMAL_BLUR_RUNNABLES.add(() -> {
                    RenderUtil.drawIsoscelesTrapezoid(logoX + clientNameLength + 25f, logoY + 1f, 7f + length, 16f, 8f, 0, Color.WHITE);
                    RenderUtil.drawRightTrapezoid(logoX + 2f, logoY + 1f, 11f, 16f, 8f, 0, Color.WHITE);
                    RenderUtil.roundedRectangleDarker(logoX, logoY, 2f, 18f, 0f, Color.WHITE);
                });

            if (bloom.getValue())
                NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                    RenderUtil.drawIsoscelesTrapezoid(logoX + clientNameLength + 25f, logoY + 1f, 7f + length, 16f, 8f, 0, new Color(0, 0, 0, 125));
                    RenderUtil.drawRightTrapezoid(logoX + 2f, logoY + 1f, 11f, 16f, 8f, 0, new Color(0, 0, 0, 125));
                    RenderUtil.drawParallelogram(logoX + clientNameLength + 41f + length, logoY, 0.3f, 18f, -8f, 0, new Color(0, 0, 0, 125));
                    RenderUtil.drawParallelogram(logoX + 16f, logoY, clientNameLength + 6f, 18f, 8f, 0, new Color(0, 0, 0, 125));
                    RenderUtil.roundedRectangleDarker(logoX, logoY, 2f, 18f, 0f, new Color(0, 0, 0, 125));
                });

            NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                RenderUtil.drawIsoscelesTrapezoid(logoX + clientNameLength + 25f, logoY + 1f, 7f + length, 16f, 8f, 0, new Color(0, 0, 0, logoAlpha.getValue().intValue()));
                RenderUtil.drawRightTrapezoid(logoX + 2f, logoY + 1f, 11f, 16f, 8f, 0, new Color(0, 0, 0, logoAlpha.getValue().intValue()));
                RenderUtil.drawGradientParallelogram(logoX + clientNameLength + 41f + length, logoY, 0.3f, 18f, -8f, 0, color1, color2, logoVertical.getValue());
                RenderUtil.drawGradientParallelogram(logoX + 16f, logoY, clientNameLength + 6f, 18f, 8f, 0, color1, color2, logoVertical.getValue());
                RenderUtil.drawRoundedGradientRectDarker(logoX, logoY, 2f, 18f, 0f, color1, color2, logoVertical.getValue());
                icon22.drawString("U", logoX + 3.5f, logoY + 6.5f, getTheme().getAccentColor(new Vector2d(0, 100)).getRGB());
                productSansMedium22.drawString(Client.NAME, logoX + 23.5f, logoY + 5.5f, new Color(255, 255, 255).getRGB());
                productSansMedium20.drawString(displayText, logoX + clientNameLength + 32f, logoY + 6.5f, Color.white.getRGB());
                getParent().logoDrag.setScale(new Vector2d(clientNameLength + 45f + length, 17));
            });
        }
    };
}
