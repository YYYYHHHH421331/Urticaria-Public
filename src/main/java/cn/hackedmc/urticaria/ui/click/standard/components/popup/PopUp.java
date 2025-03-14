package cn.hackedmc.urticaria.ui.click.standard.components.popup;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.ui.click.standard.RiseClickGUI;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.util.vector.Vector2f;

import java.awt.*;

/**
 * Author: Alan
 * Date: 28/03/2022
 */

public class PopUp implements InstanceAccess {
    public Vector2f scale;

    public void draw() {
        final RiseClickGUI clickGUI = Client.INSTANCE.getStandardClickGUI();

        final double x = clickGUI.position.x;
        final double y = clickGUI.position.y;
        final double width = clickGUI.scale.x;
        final double height = clickGUI.scale.y;

//        RenderUtil.rectangle(x, y, width, height, new Color(0, 0, 0, 100));

        if (scale == null) return;

        RenderUtil.dropShadow(60, (float) (x + width / 2 - scale.x / 2), (float) (y + height / 2 - scale.y / 2),
                scale.x, scale.y, 50, 34);

        RenderUtil.roundedRectangle(x + width / 2 - scale.x / 2, y + height / 2 - scale.y / 2, scale.x, scale.y,
                9, new Color(0, 0, 0, 230));


    }
}
