package cn.hackedmc.urticaria.ui.menu.component;

import cn.hackedmc.urticaria.ui.menu.MenuColors;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import lombok.Getter;

@Getter
public class MenuComponent implements InstanceAccess, MenuColors {

    protected double x;
    protected final double y;
    private final double width;
    private final double height;

    public MenuComponent(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
