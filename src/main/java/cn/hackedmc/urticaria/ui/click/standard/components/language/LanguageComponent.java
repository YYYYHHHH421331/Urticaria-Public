package cn.hackedmc.urticaria.ui.click.standard.components.language;

import cn.hackedmc.urticaria.util.gui.GUIUtil;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.localization.Locale;
import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.util.font.FontManager;
import cn.hackedmc.urticaria.util.render.ColorUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.util.vector.Vector2f;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.ResourceLocation;

/**
 * @author Hazsi
 * @since 10/31/22
 */
@Getter
@RequiredArgsConstructor
public class LanguageComponent implements InstanceAccess {
    private final Locale locale;
    private final String localName, englishName;
    
    private double lastY;

    public void draw(double y) {
        final Vector2f position = getStandardClickGUI().getPosition();
        final Vector2f scale = getStandardClickGUI().getScale();
        final double sidebar = getStandardClickGUI().getSidebar().sidebarWidth;

        RenderUtil.roundedRectangle(position.getX() + sidebar + 8, position.getY() + y, 285,
                38, 6, getStandardClickGUI().getSidebarColor());

        // Draw locale english name
        FontManager.getNunito(20).drawString(this.englishName, position.getX() + sidebar + 18, position.getY() + y + 9,
                Client.INSTANCE.getLocale().equals(this.locale) ? getTheme().getAccentColor(new Vector2d(0, position.y / 5)).getRGB() :
                        getStandardClickGUI().getFontColor().getRGB());

        // Draw locale native name
        FontManager.getNunito(17).drawString(this.localName, position.getX() + sidebar + 18,
                position.getY() + y + 24, ColorUtil.withAlpha(getStandardClickGUI().getFontColor(), 100).hashCode());

        // Draw flag
        RenderUtil.image(new ResourceLocation("urticaria/icons/language/" + locale.getFile() + ".png"),
                position.getX() + sidebar + FontManager.getNunito(20).width(this.englishName) + 25, position.getY() + y + 5, 15, 15);

        this.lastY = y;
    }
    
    public void click(double mouseX, double mouseY) {
        final Vector2f position = getStandardClickGUI().getPosition();
        final Vector2f scale = getStandardClickGUI().getScale();
        final double sidebar = getStandardClickGUI().getSidebar().sidebarWidth;
        
        if (GUIUtil.mouseOver(position.getX() + sidebar + 8, position.getY() + lastY,
                285, 38, mouseX, mouseY)) {

            Client.INSTANCE.setLocale(this.locale);
        }
    }
}