package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.component.impl.player.RotationComponent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.render.Render3DEvent;
import cn.hackedmc.urticaria.util.render.ColorUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.util.ArrayList;

@ModuleInfo(name = "module.render.projectiles.name", description = "module.render.projectiles.description", category = Category.RENDER)
public class Projectiles extends Module {
    private final ModeValue colorMode = new ModeValue("Color Mode", this)
            .add(new SubMode("Normal"))
            .add(new SubMode("Bow Power"))
            .setDefault("Normal");

    private static Color interpolateHSB(float process) {
        float[] startHSB = Color.RGBtoHSB(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue(), null);
        float[] endHSB = Color.RGBtoHSB(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), null);

        float brightness = (startHSB[2] + endHSB[2]) / 2;
        float saturation = (startHSB[1] + endHSB[1]) / 2;

        float hueMax = Math.max(startHSB[0], endHSB[0]);
        float hueMin = Math.min(startHSB[0], endHSB[0]);

        float hue = (hueMax - hueMin) * process + hueMin;
        return Color.getHSBColor(hue, saturation, brightness);
    }

    @EventLink
    private final Listener<Render3DEvent> onRender3D = event -> {
        EntityPlayerSP thePlayer = mc.thePlayer;
        if (thePlayer == null) return;
        WorldClient theWorld = mc.theWorld;
        if (theWorld == null) return;

        ItemStack heldItem = thePlayer.getHeldItem();
        if (heldItem == null) return;
        Item item = heldItem.getItem();
        RenderManager renderManager = mc.getRenderManager();
        boolean isBow = false;
        float motionFactor = 1.5F;
        float motionSlowdown = 0.99F;
        float gravity;
        float size;

        // Check items
        if (item instanceof ItemBow) {
            if (!thePlayer.isUsingItem()) return;

            isBow = true;
            gravity = 0.05F;
            size = 0.3F;

            // Calculate power of bow
            float power = thePlayer.getItemInUseDuration() / 20f;
            power = (power * power + power * 2F) / 3F;
            if (power < 0.1F) return;
            if (power > 1F) power = 1F;

            motionFactor = power * 3F;
        } else if (item instanceof ItemFishingRod) {
            gravity = 0.04F;
            size = 0.25F;
            motionSlowdown = 0.92F;
        } else if (item instanceof ItemPotion && ItemPotion.isSplash(heldItem.getMetadata())) {
            gravity = 0.05F;
            size = 0.25F;
            motionFactor = 0.5F;
        } else {
            if (!(item instanceof ItemSnowball) && !(item instanceof ItemEnderPearl) && !(item instanceof ItemEgg)) return;
            gravity = 0.03F;
            size = 0.25F;
        }

        // Yaw and pitch of player
        float yaw = RotationComponent.lastServerRotations != null ?
                RotationComponent.lastServerRotations.x :
                thePlayer.rotationYaw;

        float pitch = RotationComponent.lastServerRotations != null ?
                RotationComponent.lastServerRotations.y :
                thePlayer.rotationPitch;

        float yawRadians = yaw / 180f * (float) Math.PI;
        float pitchRadians = pitch / 180f * (float) Math.PI;

        // Positions
        double posX = renderManager.renderPosX - Math.cos(yawRadians) * 0.16F;
        double posY = renderManager.renderPosY + thePlayer.getEyeHeight() - 0.10000000149011612;
        double posZ = renderManager.renderPosZ - Math.sin(yawRadians) * 0.16F;

        // Motions
        double motionX = -Math.sin(yawRadians) * Math.cos(pitchRadians) * (isBow ? 1.0 : 0.4);
        double motionY = -Math.sin((pitch + (item instanceof ItemPotion && ItemPotion.isSplash(heldItem.getMetadata()) ? -20 : 0)) / 180f * 3.1415927f) * (isBow ? 1.0 : 0.4);
        double motionZ = Math.cos(yawRadians) * Math.cos(pitchRadians) * (isBow ? 1.0 : 0.4);

        double distance = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        motionX /= distance;
        motionY /= distance;
        motionZ /= distance;
        motionX *= motionFactor;
        motionY *= motionFactor;
        motionZ *= motionFactor;

        // Landing
        MovingObjectPosition landingPosition = null;
        boolean hasLanded = false;
        boolean hitEntity = false;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        // Start drawing of path
        GL11.glDepthMask(false);
        RenderUtil.enableGlCap(GL11.GL_BLEND, GL11.GL_LINE_SMOOTH);
        RenderUtil.disableGlCap(GL11.GL_DEPTH_TEST, GL11.GL_ALPHA_TEST, GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        switch (colorMode.getValue().getName().toLowerCase()) {
            case "normal":
                ColorUtil.glColor(getTheme().getAccentColor(new Vector2d(0, 100)));
                break;
            case "bowpower":
                ColorUtil.glColor(interpolateHSB((motionFactor / 30) * 10));
                break;
        }
        GL11.glLineWidth(2f);

        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

        while (!hasLanded && posY > 0.0) {
            // Set pos before and after
            Vec3 posBefore = new Vec3(posX, posY, posZ);
            Vec3 posAfter = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);

            // Get landing position
            landingPosition = theWorld.rayTraceBlocks(posBefore, posAfter, false, true, false);

            // Set pos before and after
            posBefore = new Vec3(posX, posY, posZ);
            posAfter = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);

            // Check if arrow is landing
            if (landingPosition != null) {
                hasLanded = true;
                posAfter = new Vec3(landingPosition.hitVec.xCoord, landingPosition.hitVec.yCoord, landingPosition.hitVec.zCoord);
            }

            // Set arrow box
            AxisAlignedBB arrowBox = new AxisAlignedBB(posX - size, posY - size, posZ - size, posX + size, posY + size, posZ + size)
                    .addCoord(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0);

            int chunkMinX = (int) Math.floor((arrowBox.minX - 2.0) / 16.0);
            int chunkMaxX = (int) Math.floor((arrowBox.maxX + 2.0) / 16.0);
            int chunkMinZ = (int) Math.floor((arrowBox.minZ - 2.0) / 16.0);
            int chunkMaxZ = (int) Math.floor((arrowBox.maxZ + 2.0) / 16.0);

            // Check which entities colliding with the arrow
            java.util.List<Entity> collidedEntities = new ArrayList<>();

            for (int x = chunkMinX; x <= chunkMaxX; x++)
                for (int z = chunkMinZ; z <= chunkMaxZ; z++)
                    theWorld.getChunkFromChunkCoords(x, z)
                            .getEntitiesWithinAABBForEntity(thePlayer, arrowBox, collidedEntities, null);

            // Check all possible entities
            for (Entity possibleEntity : collidedEntities) {
                if (possibleEntity.canBeCollidedWith() && possibleEntity != thePlayer) {
                    AxisAlignedBB possibleEntityBoundingBox = possibleEntity.getEntityBoundingBox().expand(size, size, size);
                    MovingObjectPosition possibleEntityLanding = possibleEntityBoundingBox.calculateIntercept(posBefore, posAfter);
                    if (possibleEntityLanding != null) {
                        hitEntity = true;
                        hasLanded = true;
                        landingPosition = possibleEntityLanding;
                    }
                }
            }

            // Affect motions of arrow
            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            IBlockState blockState = theWorld.getBlockState(new BlockPos(posX, posY, posZ));

            // Check if next position is water
            if (blockState.getBlock().getMaterial() == Material.water) {
                // Update motion
                motionX *= 0.6;
                motionY *= 0.6;
                motionZ *= 0.6;
            } else {
                // Update motion
                motionX *= motionSlowdown;
                motionY *= motionSlowdown;
                motionZ *= motionSlowdown;
            }

            motionY -= gravity;

            // Draw path
            worldRenderer.pos(posX - renderManager.renderPosX, posY - renderManager.renderPosY, posZ - renderManager.renderPosZ).endVertex();
        }

        // End the rendering of the path
        tessellator.draw();
        GL11.glPushMatrix();
        GL11.glTranslated(posX - renderManager.renderPosX, posY - renderManager.renderPosY, posZ - renderManager.renderPosZ);

        if (landingPosition != null) {
            // Switch rotation of hit cylinder of the hit axis
            switch (landingPosition.sideHit.getAxis().ordinal()) {
                case 0:
                    GL11.glRotatef(90F, 0F, 0F, 1F);
                    break;
                case 2:
                    GL11.glRotatef(90F, 1F, 0F, 0F);
                    break;
            }

            // Check if hitting an entity
            if (hitEntity)
                ColorUtil.glColor(new Color(255, 0, 0, 150));
        }

        // Rendering hit cylinder
        GL11.glRotatef(-90F, 1F, 0F, 0F);

        Cylinder cylinder = new Cylinder();
        cylinder.setDrawStyle(GLU.GLU_LINE);
        cylinder.draw(0.2F, 0F, 0F, 60, 1);

        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        RenderUtil.resetCaps();
        GL11.glColor4f(1F, 1F, 1F, 1F);
    };
}
