package cn.hackedmc.urticaria.util.font;

public abstract class Font {
    public abstract int drawString(String text, double x, double y, int color, boolean dropShadow);

    public abstract int drawString(final String text, final double x, final double y, final int color);

    public abstract int drawStringWithShadow(final String text, final double x, final double y, final int color);

    public abstract int width(String text);
    public abstract void drawCenteredString(final String text, final double x, final double y, final int color,boolean shadow);

    public abstract void drawCenteredString(final String text, final double x, final double y, final int color);

    public abstract int drawRightString(final String text, final double x, final double y, final int color);

    public abstract float height();
}
