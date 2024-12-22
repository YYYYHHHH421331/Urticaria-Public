package cn.hackedmc.urticaria.util.font.impl.game;

import cn.hackedmc.urticaria.util.font.impl.minecraft.FontRenderer;
import cn.hackedmc.urticaria.util.render.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Random;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;

public class GameFontRenderer extends FontRenderer {
    public static boolean SecondaryFontAntiAliasing = true;

    private static final HashMap<Integer, Font> SECONDARY_FONT_MAP = new HashMap<>();
    private static final String COLOR_CODE = "0123456789abcdefklmnor";
    private static final int[] COLORS = new int[32];
    private static final int SHADOW_COLOR = ColorUtil.getRGB(0,0,0,180);
    private static final Random FONT_RANDOM = new Random();
    private static final String RANDOM_STRING = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";
    private static final int MARGIN = 2;
    private final Glyph[] glyphs = new Glyph[65536];
    private final Font font;
    private final Font secondaryFont;
    private final int fontSize;
    private final int imageSize;
    private final int halfHeight;
    private final boolean antiAliasing;
    private final boolean fractionalMetrics;

    static {
        for (int i = 0; i < COLORS.length; i++) {
            final int offset = (i >> 3 & 1) * 85;

            int red = (i >> 2 & 1) * 170 + offset;
            int green = (i >> 1 & 1) * 170 + offset;
            int blue = (i & 1) * 170 + offset;

            if (i == 6) {
                red += 85;
            }

            if (i >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            COLORS[i] = (red & 255) << 16 | (green & 255) << 8 | blue & 255;
        }
    }

    public GameFontRenderer(Font font) {
        this(font, true, true);
    }

    public GameFontRenderer(Font font, boolean antiAliasing, boolean fractionalMetrics) {
        super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().getTextureManager(), false);
        this.font = font;
        this.fontSize = font.getSize();
        this.imageSize = font.getSize() + (font.getSize() / 4);
        this.halfHeight = font.getSize() / new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        this.antiAliasing = antiAliasing;
        this.fractionalMetrics = fractionalMetrics;
        this.secondaryFont = getSecondaryFont();

        this.FONT_HEIGHT = halfHeight;
    }

    public int getInitializedGlyphCount() {
        int i = 0;

        for (Glyph glyph : glyphs)
            if (glyph != null)
                i++;

        return i;
    }

    private Font getSecondaryFont() {
        Font secondaryFont = SECONDARY_FONT_MAP.get(fontSize);

        if (secondaryFont == null) {
            secondaryFont = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);

            SECONDARY_FONT_MAP.put(fontSize, secondaryFont);
        }

        return secondaryFont;
    }

    @Override
    public int drawString(String text, double x, double y, int color, boolean dropShadow) {
        return drawString(text, (float) x, (float) y - 2, color, dropShadow);
    }

    @Override
    public int drawString(String text, double x, double y, int color) {
        return drawString(text, x, y, color, false);
    }

    @Override
    public int drawStringWithShadow(String text, double x, double y, int color) {
        return drawString(text, x, y, color, true);
    }

    public int width(String s) {
        if (s == null || s.isEmpty()) return 0;

        final int scaledFactor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();

        int stringWidth = 0;

        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);

            if ((c == '§' || isEmojiCharacter(c)) && i < s.length() - 1) {
                i++;
            } else {
                stringWidth += getGlyph(c).width;
            }
        }

        return stringWidth / scaledFactor;
    }

    @Override
    public void drawCenteredString(String text, double x, double y, int color, boolean shadow) {
        if (shadow)
            drawCenteredStringWithShadow(text, (float) x, (float) y, color);
        else
            drawCenteredString(text, (float) x, (float) y, color);
    }

    @Override
    public void drawCenteredString(String text, double x, double y, int color) {
        drawCenteredString(text, (float) x, (float) y, color);
    }

    @Override
    public int drawRightString(String text, double x, double y, int color) {
        return drawString(text, x - this.width(text), y, color);
    }

    @Override
    public float height() {
        return getHeight();
    }

    public int getStringWidth(String s) {
        if (s == null || s.isEmpty()) return 0;

        final int scaledFactor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();

        int stringWidth = 0;

        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);

            if ((c == '§' || isEmojiCharacter(c)) && i < s.length() - 1) {
                i++;
            } else {
                stringWidth += getGlyph(c).width;
            }
        }

        return stringWidth / scaledFactor;
    }

    public int getStringWidthDirectly(String s) {
        if (s == null || s.isEmpty()) return 0;

        final int scaledFactor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();

        int stringWidth = 0;

        for (int i = 0; i < s.length(); i++) {
            stringWidth += getGlyph(s.charAt(i)).width;
        }

        return stringWidth / scaledFactor;
    }

    public int getCharWidth(char c) {
        return getGlyph(c).renderWidth;
    }

    public int getHeight() {
        final int scaledFactor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        return fontSize / scaledFactor;
    }

    public void drawCenteredStringWithShadow(String s, float x, float y, int color) {
        drawStringWithShadow(s, x - (getStringWidth(s) / 2.0F), y, color);
    }

    public void drawCenteredString(String s, float x, float y, int color) {
        renderString(s, x - getStringWidth(s) / 2.0F, y, color, false);
    }

    public void drawStringWithShadowDirectly(String s, float x, float y, int color) {
        final int alpha = ColorUtil.getAlpha(color);

        drawStringDirectly(s, x + 0.5F, y + 0.5F, alpha < 200 ? ColorUtil.reAlpha(SHADOW_COLOR, alpha) : SHADOW_COLOR);
        drawStringDirectly(s, x, y, color);
    }

    public int drawStringDirectly(String s, float x, float y, int color) {
        if (s == null || s.isEmpty()) return 0;

        float scaledFactor = (float) new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();

        if ((color & -67108864) == 0) {
            color |= -16777216;
        }

        preDraw();
        ColorUtil.color(color);

        x *= scaledFactor;
        y = (y - MARGIN) * scaledFactor;

        for (int i = 0; i < s.length(); i++) {
            final Glyph glyph = getGlyph(s.charAt(i));

            glyph.draw(x, y, false);

            x += glyph.width;
        }

        postDraw();

        return (int) x;
    }

    public void drawStringWithOutline(String s, float x, float y, int color) {
        renderString(s, x + 0.5F, y, 0, false);
        renderString(s, x - 0.5F, y, 0, false);
        renderString(s, x, y + 0.5F, 0, false);
        renderString(s, x, y - 0.5F, 0, false);
        renderString(s, x, y, color, false);
    }

    public int drawStringWithShadow(String s, float x, float y, int color) {
        final int alpha = ColorUtil.getAlpha(color);

        return Math.max(renderString(s, x + 0.5F, y + 0.5F, alpha < 200 ? ColorUtil.reAlpha(SHADOW_COLOR, alpha) : SHADOW_COLOR, true),
                renderString(s, x, y, color, false));
    }

    public int drawString(String s,int x,int y,int color) {
        return renderString(s, x, y, color, false);
    }

    public int drawString(String s, float x, float y, int color, boolean shadow) {
        if (shadow) {
            return this.drawStringWithShadow(s, x, y, color);
        } else {
            return this.renderString(s, x, y, color, false);
        }
    }

    public int renderString(String s, float x, float y, int color, boolean shadow) {
        if (s == null || s.isEmpty()) return 0;

        int renderX = 0;
        float scaledFactor = (float) new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();

        if ((color & -67108864) == 0) {
            color |= -16777216;
        }

        preDraw();
        ColorUtil.color(color);

        x *= scaledFactor;
        y = (y - MARGIN) * scaledFactor;

        boolean randomStyle = false;
        boolean bold = false;
        boolean italic = false;
        boolean strikethrough = false;
        boolean underline = false;

        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            final boolean isEmojiCharacter = isEmojiCharacter(c);

            if ((c == '§' || isEmojiCharacter) && i < s.length() - 1) {
                i++;

                if (!isEmojiCharacter) {
                    int colorIndex = COLOR_CODE.indexOf(s.charAt(i));

                    switch (colorIndex) {
                        case 16:        // 乱码
                            randomStyle = true;

                            break;
                        case 17:        // 加粗
                            bold = true;

                            break;
                        case 18:        // 删除线
                            strikethrough = true;

                            break;
                        case 19:        // 下划线
                            underline = true;

                            break;
                        case 20:        // 斜体
                            italic = true;

                            break;
                        case 21:        // 重置
                            randomStyle = false;
                            bold = false;
                            italic = false;
                            underline = false;
                            strikethrough = false;

                            ColorUtil.color(color);

                            break;
                        default:
                            if (!shadow) {
                                if (colorIndex == -1) {
                                    colorIndex = 15;
                                }

                                final int finalColor = COLORS[colorIndex];

                                ColorUtil.color(ColorUtil.getRed(finalColor), ColorUtil.getGreen(finalColor), ColorUtil.getBlue(finalColor), ColorUtil.getAlpha(color));
                            }

                            break;
                    }
                }
            } else {
                char targetChar = c;

                if (randomStyle && RANDOM_STRING.indexOf(c) != -1) {
                    final int charWidth = getCharWidth(c);
                    int index;

                    do {
                        index = FONT_RANDOM.nextInt(RANDOM_STRING.length());
                        targetChar = RANDOM_STRING.charAt(index);

                    } while (charWidth != getCharWidth(targetChar));
                }

                final Glyph glyph = getGlyph(targetChar);

                if (shadow) {
                    drawGlyph(glyph, x, y, bold, false, false, italic);
                } else {
                    drawGlyph(glyph, x, y, bold, strikethrough, underline, italic);
                }

                x += glyph.width;
            }
        }

        postDraw();

        return (int) x / new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
    }

    public void drawCharWithShadow(char c, double x, double y, int color) {
        final int alpha = ColorUtil.getAlpha(color);

        drawChar(c, x + 0.5, y + 0.5, alpha < 200 ? ColorUtil.reAlpha(SHADOW_COLOR, alpha) : SHADOW_COLOR);
        drawChar(c, x, y, color);
    }

    public void drawChar(char c, double x, double y, int color) {
        preDraw();
        ColorUtil.color(color);

        x *= 2.0;
        y *= 2.0;

        getGlyph(c).draw(x, y, false);

        postDraw();
    }

    private void drawGlyph(Glyph glyph, double x, double y, boolean bold, boolean strikethrough, boolean underline, boolean italic) {
        if (bold) {
            glyph.draw(x + 1,y,italic);
        }

        glyph.draw(x,y,italic);

        if (strikethrough) {
            final double mid = y + halfHeight + 2;

            drawLine(x, mid - 1, x + glyph.width, mid + 1);
        }

        if (underline) {
            drawLine(x, y + fontSize + 1, x + glyph.width, y + fontSize + 2);
        }
    }

    private void preDraw() {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableTexture2D();
        float scaledFactor = (float) new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        GlStateManager.scale(1 / scaledFactor, 1 / scaledFactor, 1 / scaledFactor);
    }

    private void postDraw() {
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private Glyph getGlyph(char c) {
        Glyph glyph = glyphs[c];

        if (glyph == null) {
            glyphs[c] = glyph = createGlyph(c);
        }

        return glyph;
    }

    private Glyph createGlyph(char c) {
        final String s = String.valueOf(c);
        final BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = image.createGraphics();

        Font f = font;

        if (font.canDisplay(c)) {
            setRenderingHints(g, antiAliasing, fractionalMetrics);
        } else {
            setRenderingHints(g, SecondaryFontAntiAliasing, true);

            f = secondaryFont;
        }

        g.setFont(f);
        g.setColor(Color.WHITE);
        g.drawString(s, 0, fontSize);

        g.dispose();

        final FontMetrics fontMetrics = g.getFontMetrics();

        return new Glyph(image, fontMetrics.getStringBounds(s, g).getBounds().width);
    }

    private static void setRenderingHints(Graphics2D g, boolean antiAliasing, boolean fractionalMetrics) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasing ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }

    private static boolean isEmojiCharacter(char codePoint) {
        return !(codePoint == 0x0 || codePoint == 0x9 || codePoint == 0xA || codePoint == 0xD || codePoint >= 0x20 && codePoint <= 0xD7FF || codePoint >= 0xE000 && codePoint <= 0xFFFD);
    }

    private static void drawLine(double left, double top, double right, double bottom) {
        if (left < right) {
            final double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            final double j = top;
            top = bottom;
            bottom = j;
        }

        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer bufferBuilder = tessellator.getWorldRenderer();
        GlStateManager.disableTexture2D();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(left, bottom, 0.0D).endVertex();
        bufferBuilder.pos(right, bottom, 0.0D).endVertex();
        bufferBuilder.pos(right, top, 0.0D).endVertex();
        bufferBuilder.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }

    private final class Glyph {
        public final int textureID;
        public final int width;
        public final int renderWidth;

        public Glyph(BufferedImage image, int width) {
            this.textureID = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), image, false, false);
            this.width = width;
            this.renderWidth = width / new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        }

        public void draw(double x, double y,boolean italic) {
            GlStateManager.bindTexture(textureID);

            final double offset = italic ? 2.0 : 0.0;

            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            GL11.glTexCoord2d(0, 0);
            GL11.glVertex3d(x + offset, y, 0.0F);
            GL11.glTexCoord2d(0, 1);
            GL11.glVertex3d(x - offset, y + imageSize, 0.0F);
            GL11.glTexCoord2d(1, 0);
            GL11.glVertex3d(x + imageSize + offset, y, 0.0F);
            GL11.glTexCoord2d(1, 1);
            GL11.glVertex3d(x + imageSize - offset, y + imageSize, 0.0F);
            GL11.glEnd();
        }
    }
}