package cn.hackedmc.urticaria.ui.menu.component.button.impl;

import cn.hackedmc.urticaria.ui.menu.component.button.MenuButton;
import cn.hackedmc.urticaria.util.MouseUtil;
import cn.hackedmc.urticaria.util.animation.Animation;
import cn.hackedmc.urticaria.util.animation.Easing;
import cn.hackedmc.urticaria.util.font.Font;
import cn.hackedmc.urticaria.util.font.FontManager;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import net.minecraft.util.ResourceLocation;

import java.awt.*;


public class MainMenuButton  extends MenuButton {
    private final Animation animation = new Animation(Easing.EASE_OUT_QUINT, 300);
    public static final Font FONT_RENDERER = FontManager.getProductSansBold(35);

    private final ResourceLocation image;
    public String name;

    public MainMenuButton(double x, double y, Runnable runnable, String name, ResourceLocation image) {
        super(x, y, FONT_RENDERER.width(name), FONT_RENDERER.height(), runnable);
        this.name = name;
        this.image = image;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        // Runs the animation update - keep this
        super.draw(mouseX, mouseY, partialTicks);

//        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle(this.getX(), value, this.getWidth(), this.getHeight(),
//                2,new Color(0,0,0,50)));
//        NORMAL_POST_BLOOM_RUNNABLES.add(()->  RenderUtil.roundedRectangle(this.getX(), value, this.getWidth(), this.getHeight(),
//                2,new Color(0,0,0,50)));

//        RenderUtil.roundedRectangle(this.getX(), value, this.getWidth(), this.getHeight(), 2,new Color(0,0,0,50));
        if (MouseUtil.isHovered(this.getX(), this.getY(), this.getWidth(), this.getHeight(), mouseX, mouseY)) {
            animation.setEasing(Easing.EASE_OUT_QUINT);
            animation.run(FONT_RENDERER.width(this.name));
        } else {
            animation.setEasing(Easing.EASE_IN_QUINT);
            animation.run(0);
        }

       // blurShader.update();
      //  blurShader.run(ShaderRenderType.OVERLAY, partialTicks, InstanceAccess.NORMAL_BLUR_RUNNABLES);
        //RenderUtil.roundedOutlineRectangle(this.getX(), value, this.getWidth(), this.getHeight(), 0, 1, basic);
        RenderUtil.roundedRectangle(this.getX(), this.getY() + FONT_RENDERER.height(), animation.getValue(), 2, 1, new Color(255, 255, 255, 255));
        FONT_RENDERER.drawString(this.name, this.getX(),
                getY(),new Color(255,255,255,200).getRGB());
        RenderUtil.image(this.image , this.getX() - FONT_RENDERER.height() - 4, this.getY() - 4, FONT_RENDERER.height() + 4, FONT_RENDERER.height() + 4);
    }

    public void setX(float x) {
        this.x = x;
    }
}
