package cn.hackedmc.urticaria.component.impl.render.espcomponent.impl;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.render.ColorUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.component.impl.render.espcomponent.api.ESP;
import cn.hackedmc.urticaria.component.impl.render.espcomponent.api.ESPColor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class PlayerChams extends ESP implements InstanceAccess {

    public PlayerChams(ESPColor espColor) {
        super(espColor);
    }

    @Override
    public void render3D() {
        final float partialTicks = mc.timer.renderPartialTicks;
        for (final Entity entity : Client.INSTANCE.getTargetManager().getTargets(100)) {
            if (!(entity instanceof EntityPlayer)) continue;
            final EntityPlayer player = (EntityPlayer) entity;
            final Render<EntityPlayer> render = mc.getRenderManager().getEntityRenderObject(player);

            if (mc.getRenderManager() == null || render == null || (player == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) || !RenderUtil.isInViewFrustrum(player) || player.isDead) {
                continue;
            }

            final Color color = ColorUtil.withAlpha(getColor(player), 150);

            if (color.getAlpha() <= 0) {
                continue;
            }

            final double x = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
            final double y = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
            final double z = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
            final float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;

            RendererLivingEntity.setShaderBrightnessWithAlpha(color);
            render.doRender(player, x - mc.getRenderManager().renderPosX, y - mc.getRenderManager().renderPosY, z - mc.getRenderManager().renderPosZ, yaw, partialTicks);
            RendererLivingEntity.unsetShaderBrightness();

            player.hide();
        }

        RenderHelper.disableStandardItemLighting();
        mc.entityRenderer.disableLightmap();
    }
}
