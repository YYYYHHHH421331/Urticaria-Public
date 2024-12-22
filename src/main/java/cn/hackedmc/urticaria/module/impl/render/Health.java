package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@Rise
@ModuleInfo(name = "module.combat.health.name", description = "module.combat.health.description", category = Category.RENDER)
public class Health extends Module {

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (mc.thePlayer == null || mc.gameSettings.showDebugInfo) return;

        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        // Don't draw if the F3 menu is open
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/icons.png"));
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        final float maxHealth = mc.thePlayer.getMaxHealth();
        for (int n = 0; n < maxHealth / 2.0f; ++n) {
            mc.ingameGUI.drawTexturedModalRect(scaledResolution.getScaledWidth() / 2 + 1f - maxHealth / 2.0f * 9.5f / 2.0f + n * 9.5f, (scaledResolution.getScaledHeight() / 2 - 20 + 30), 16, 0, 9, 9);
        }
        final float health = mc.thePlayer.getHealth();
        for (int n2 = 0; n2 < health / 2.0f; ++n2) {
            mc.ingameGUI.drawTexturedModalRect(scaledResolution.getScaledWidth() / 2 + 1f - maxHealth / 2.0f * 9.5f / 2.0f + n2 * 9.5f, (scaledResolution.getScaledHeight() / 2 - 20 + 30), 52, 0, 9, 9);
        }
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
    };
}