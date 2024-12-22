package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.player.GUIDetectionComponent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.exploit.Disabler;
import cn.hackedmc.urticaria.module.impl.movement.InventoryMove;
import cn.hackedmc.urticaria.module.impl.movement.inventorymove.NormalInventoryMove;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.newevent.impl.render.ChestRenderEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render3DEvent;
import cn.hackedmc.urticaria.util.math.MathUtil;
import cn.hackedmc.urticaria.util.player.ItemUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.util.render.Stencil;
import cn.hackedmc.urticaria.util.render.StencilUtil;
import cn.hackedmc.urticaria.util.shader.base.RiseShaderProgram;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.BoundsNumberValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.block.BlockChest;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import util.time.StopWatch;

import java.awt.*;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;


@Rise
@ModuleInfo(name = "module.player.stealer.name", description = "module.player.stealer.description", category = Category.PLAYER)
public class Stealer extends Module {
    public static Stealer INSTANCE;
    private final ModeValue mode = new ModeValue("Click Time", this)
            .add(new SubMode("Normal"))
            .add(new SubMode("Fast"))
            .setDefault("Normal");
    private final BooleanValue sendPacket = new BooleanValue("Only Packet", this, false);
    private final BoundsNumberValue delay = new BoundsNumberValue("Delay", this, 100, 150, 0, 500, 25);
    public final BooleanValue silent = new BooleanValue("Silent", this, false);
    private final BooleanValue ignoreTrash = new BooleanValue("Ignore Trash", this, true);
    private final BooleanValue onlyBest = new BooleanValue("Only Best", this, false);
    private final BooleanValue timeCheck = new BooleanValue("No Open Delay", this, false);
    private final StopWatch stopwatch = new StopWatch();
    public BlockPos blockPos;
    public long showTime;
    public BlockPos animatedPos;
    private long nextClick;
    private int lastClick;
    private int lastSteal;
    private final ArrayList<Integer> shouldTake = new ArrayList<>();

    private GuiChest guiChest;
    private TileEntityChest chest;
    private Framebuffer framebuffer = new Framebuffer(1, 1, false);

    @Override
    protected void onEnable() {
        shouldTake.clear();
        guiChest = null;
        chest = null;
    }

    @EventLink
    private final Listener<PacketSendEvent> onPacketSend = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C08PacketPlayerBlockPlacement) {
            final C08PacketPlayerBlockPlacement wrapped = (C08PacketPlayerBlockPlacement) packet;

            if (mc.theWorld.getBlockState(wrapped.getPosition()).getBlock() instanceof BlockChest)
                blockPos = wrapped.getPosition();
        }
    };

    @EventLink(value = Priorities.VERY_LOW)
    private final Listener<Render2DEvent> onRender2D = event -> {
        if (this.silent.getValue() && this.guiChest != null && this.chest != null && (blockPos != null || animatedPos != null)) {
            final int length = chest.getSizeInventory() / 9;
            framebuffer = RenderUtil.createFrameBuffer(framebuffer, true);
            framebuffer.framebufferClear();
            framebuffer.bindFramebuffer(false);
            GlStateManager.enableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();

            for (int yi = 1; yi <= length; yi++) {
                for (int xi = 1; xi <= 9; xi++) {
                    final ItemStack itemStack = guiChest.inventorySlots.inventorySlots.get((yi - 1) * 9 + xi - 1).getStack();

                    if (itemStack != null) {
                        mc.getRenderItem().renderItemIntoGUI3D(itemStack, ((xi - 1) * 22) + 4, ((yi - 1) * 22) + 4);
                        mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, itemStack, (xi - 1) * 22 + 4, (yi - 1) * 22 + 4, null);
                    }
                }
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            framebuffer.unbindFramebuffer();
            mc.getFramebuffer().bindFramebuffer(false);
        }

        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (mode.getValue().getName().equalsIgnoreCase("fast"))
            work();
    };

    @EventLink
    private final Listener<Render3DEvent> onRender3D = event -> {
        if (!silent.getValue() || (blockPos == null && animatedPos == null) || (!(mc.currentScreen instanceof GuiChest) && animatedPos == null)) {
            showTime = System.currentTimeMillis();

            return;
        }

        mc.theWorld.loadedTileEntityList.forEach(entity -> {
            if (entity instanceof TileEntityChest) {
                this.chest = (TileEntityChest) entity;

                if (blockPos != null) {
                    if (!(mc.currentScreen instanceof GuiChest)) return;

                    this.guiChest = (GuiChest) mc.currentScreen;

                    if (chest.getPos().equals(blockPos)) {
                        final int length = chest.getSizeInventory() / 9;
                        final RenderManager renderManager = mc.getRenderManager();

                        final double posX = (blockPos.getX() + 0.5) - renderManager.renderPosX;
                        final double posY = blockPos.getY() - renderManager.renderPosY;
                        final double posZ = (blockPos.getZ() + 0.5) - renderManager.renderPosZ;

                        GL11.glPushMatrix();
                        GL11.glTranslated(posX, posY, posZ);
                        GL11.glRotated(-mc.getRenderManager().playerViewY, 0F, 1F, 0F);
                        GL11.glRotated(-mc.getRenderManager().playerViewX, -1F, 0F, 0F);
                        GL11.glScaled(Math.max((showTime - System.currentTimeMillis()) / 10000.0, -0.015), Math.max((showTime - System.currentTimeMillis()) / 10000.0, -0.015), Math.min((System.currentTimeMillis() - showTime) / 10000.0, 0.015));

                        glDisable(GL_DEPTH_TEST);
                        glDepthMask(false);
                        GlStateManager.enableBlend();
                        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

                        final int x = -99;
                        final int y = -105 - (Math.round(length / 2F) * 22);
                        final int width = 200;
                        final int height = length * 22 + 2;

                        RenderUtil.roundedRectangle(x, y, width, height, 6, new Color(255, 255, 255, 140));

                        for (int yi = 1; yi <= length; yi++) {
                            for (int xi = 1; xi <= 9; xi++) {
//                                final ItemStack itemStack = guiChest.inventorySlots.inventorySlots.get((yi - 1) * 9 + xi - 1).getStack();

                                RenderUtil.roundedRectangle(x + ((xi - 1) * 22) + 2, y + ((yi - 1) * 22) + 2, 20, 20, 3, new Color(255, 255, 255, 76));
//                                if (itemStack != null) {
//                                    mc.getRenderItem().renderItemIntoGUI3D(itemStack, ((xi - 1) * 22) + 4, ((yi - 1) * 22) + 4);
//                                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, itemStack, (xi - 1) * 22 + 4, (yi - 1) * 22 + 4, null);
//                                }
                            }
                        }

                        UI_BLOOM_RUNNABLES.add(() -> {
                            GL11.glPushMatrix();
                            GL11.glTranslated(posX, posY, posZ);
                            GL11.glRotated(-mc.getRenderManager().playerViewY, 0F, 1F, 0F);
                            GL11.glRotated(-mc.getRenderManager().playerViewX, -1F, 0F, 0F);
                            GL11.glScaled(Math.max((showTime - System.currentTimeMillis()) / 10000.0, -0.015), Math.max((showTime - System.currentTimeMillis()) / 10000.0, -0.015), Math.min((System.currentTimeMillis() - showTime) / 10000.0, 0.015));

                            RenderUtil.roundedRectangle(x, y, width, height, 6, new Color(0, 0, 0, 140));

                            GL11.glPopMatrix();
                        });

                        NORMAL_BLUR_RUNNABLES.add(() -> {
                            GL11.glPushMatrix();
                            GL11.glTranslated(posX, posY, posZ);
                            GL11.glRotated(-mc.getRenderManager().playerViewY, 0F, 1F, 0F);
                            GL11.glRotated(-mc.getRenderManager().playerViewX, -1F, 0F, 0F);
                            GL11.glScaled(Math.max((showTime - System.currentTimeMillis()) / 10000.0, -0.015), Math.max((showTime - System.currentTimeMillis()) / 10000.0, -0.015), Math.min((System.currentTimeMillis() - showTime) / 10000.0, 0.015));

                            Stencil.write(false);
                            GlStateManager.translate(0, 0, -0.001);
                            for (int yi = 1; yi <= length; yi++) {
                                for (int xi = 1; xi <= 9; xi++) {
//                                final ItemStack itemStack = guiChest.inventorySlots.inventorySlots.get((yi - 1) * 9 + xi - 1).getStack();

                                    RenderUtil.roundedRectangle(x + ((xi - 1) * 22) + 2, y + ((yi - 1) * 22) + 2, 20, 20, 3, Color.BLACK);
//                                if (itemStack != null) {
//                                    mc.getRenderItem().renderItemIntoGUI3D(itemStack, ((xi - 1) * 22) + 4, ((yi - 1) * 22) + 4);
//                                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, itemStack, (xi - 1) * 22 + 4, (yi - 1) * 22 + 4, null);
//                                }
                                }
                            }
                            GlStateManager.translate(0, 0, 0.001);
                            Stencil.erase(true);
                            StencilUtil.bindReadStencilBuffer(0);
                            RenderUtil.roundedRectangle(x, y, width, height, 6, Color.BLACK);
                            Stencil.dispose();

                            GL11.glPopMatrix();
                        });

//                        GlStateManager.translate(0, 0, -2D);
                        GlStateManager.bindTexture(framebuffer.framebufferTexture);
                        RiseShaderProgram.drawQuad(x, y, framebuffer.framebufferTextureWidth / (double) mc.scaledResolution.getScaleFactor(), framebuffer.framebufferTextureHeight / (double) mc.scaledResolution.getScaleFactor());

//                        GlStateManager.enableRescaleNormal();
//                        RenderHelper.enableGUIStandardItemLighting();
//
//                        for (int yi = 1;yi <= length;yi++) {
//                            for (int xi = 1;xi <= 9;xi++) {
//                                final ItemStack itemStack = guiChest.inventorySlots.inventorySlots.get(yi * xi - 1).getStack();
//
//                                if (itemStack != null) {
//                                    mc.getRenderItem().renderItemIntoGUI3D(itemStack, x + ((xi - 1) * 18), y + ((yi - 1) * 18));
//                                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, itemStack, x + ((xi - 1) * 18), y + ((yi - 1) * 18), null);
//                                }
//                            }
//                        }
//
//                        RenderHelper.disableStandardItemLighting();
//                        GlStateManager.disableRescaleNormal();

                        glDepthMask(true);
                        glEnable(GL_DEPTH_TEST);
                        glDisable(GL_BLEND);

                        GL11.glPopMatrix();
                    }
                } else if (guiChest != null) {
                    // GL11.glScaled(Math.min((System.currentTimeMillis() - showTime - 150) / 10000.0, 0), Math.min((System.currentTimeMillis() - showTime - 150) / 10000.0, 0), Math.max((showTime - System.currentTimeMillis() + 150) / 10000.0, 0));
                    if (System.currentTimeMillis() - showTime <= 150) {
                        if (chest.getPos().equals(animatedPos)) {
                            final int length = chest.getSizeInventory() / 9;
                            final RenderManager renderManager = mc.getRenderManager();

                            final double posX = (animatedPos.getX() + 0.5) - renderManager.renderPosX;
                            final double posY = animatedPos.getY() - renderManager.renderPosY;
                            final double posZ = (animatedPos.getZ() + 0.5) - renderManager.renderPosZ;

                            GL11.glPushMatrix();
                            GL11.glTranslated(posX, posY, posZ);
                            GL11.glRotated(-mc.getRenderManager().playerViewY, 0F, 1F, 0F);
                            GL11.glRotated(-mc.getRenderManager().playerViewX, -1F, 0F, 0F);
                            GL11.glScaled(Math.min((System.currentTimeMillis() - showTime - 150) / 10000.0, 0), Math.min((System.currentTimeMillis() - showTime - 150) / 10000.0, 0), Math.max((showTime - System.currentTimeMillis() + 150) / 10000.0, 0));

                            glDisable(GL_DEPTH_TEST);
                            glDepthMask(false);
                            GlStateManager.enableBlend();
                            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

                            final int x = -99;
                            final int y = -105 - (Math.round(length / 2F) * 22);
                            final int width = 200;
                            final int height = length * 22 + 2;

                            RenderUtil.roundedRectangle(x, y, width, height, 6, new Color(255, 255, 255, 140));

                            for (int yi = 1; yi <= length; yi++) {
                                for (int xi = 1; xi <= 9; xi++) {
//                                final ItemStack itemStack = guiChest.inventorySlots.inventorySlots.get((yi - 1) * 9 + xi - 1).getStack();

                                    RenderUtil.roundedRectangle(x + ((xi - 1) * 22) + 2, y + ((yi - 1) * 22) + 2, 20, 20, 3, new Color(255, 255, 255, 76));
//                                if (itemStack != null) {
//                                    mc.getRenderItem().renderItemIntoGUI3D(itemStack, ((xi - 1) * 22) + 4, ((yi - 1) * 22) + 4);
//                                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, itemStack, (xi - 1) * 22 + 4, (yi - 1) * 22 + 4, null);
//                                }
                                }
                            }

                            UI_BLOOM_RUNNABLES.add(() -> {
                                GL11.glPushMatrix();
                                GL11.glTranslated(posX, posY, posZ);
                                GL11.glRotated(-mc.getRenderManager().playerViewY, 0F, 1F, 0F);
                                GL11.glRotated(-mc.getRenderManager().playerViewX, -1F, 0F, 0F);
                                GL11.glScaled(Math.min((System.currentTimeMillis() - showTime - 150) / 10000.0, 0), Math.min((System.currentTimeMillis() - showTime - 150) / 10000.0, 0), Math.max((showTime - System.currentTimeMillis() + 150) / 10000.0, 0));

                                RenderUtil.roundedRectangle(x, y, width, height, 6, new Color(0, 0, 0, 140));

                                GL11.glPopMatrix();
                            });

                            NORMAL_BLUR_RUNNABLES.add(() -> {
                                GL11.glPushMatrix();
                                GL11.glTranslated(posX, posY, posZ);
                                GL11.glRotated(-mc.getRenderManager().playerViewY, 0F, 1F, 0F);
                                GL11.glRotated(-mc.getRenderManager().playerViewX, -1F, 0F, 0F);
                                GL11.glScaled(Math.min((System.currentTimeMillis() - showTime - 150) / 10000.0, 0), Math.min((System.currentTimeMillis() - showTime - 150) / 10000.0, 0), Math.max((showTime - System.currentTimeMillis() + 150) / 10000.0, 0));

                                Stencil.write(false);
                                GlStateManager.translate(0, 0, -0.001);
                                for (int yi = 1; yi <= length; yi++) {
                                    for (int xi = 1; xi <= 9; xi++) {
//                                final ItemStack itemStack = guiChest.inventorySlots.inventorySlots.get((yi - 1) * 9 + xi - 1).getStack();

                                        RenderUtil.roundedRectangle(x + ((xi - 1) * 22) + 2, y + ((yi - 1) * 22) + 2, 20, 20, 3, Color.BLACK);
//                                if (itemStack != null) {
//                                    mc.getRenderItem().renderItemIntoGUI3D(itemStack, ((xi - 1) * 22) + 4, ((yi - 1) * 22) + 4);
//                                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, itemStack, (xi - 1) * 22 + 4, (yi - 1) * 22 + 4, null);
//                                }
                                    }
                                }
                                GlStateManager.translate(0, 0, 0.001);
                                Stencil.erase(true);
                                StencilUtil.bindReadStencilBuffer(0);
                                RenderUtil.roundedRectangle(x, y, width, height, 6, Color.BLACK);
                                Stencil.dispose();

                                GL11.glPopMatrix();
                            });

//                        GlStateManager.translate(0, 0, -2D);
                            GlStateManager.bindTexture(framebuffer.framebufferTexture);
                            RiseShaderProgram.drawQuad(x, y, framebuffer.framebufferTextureWidth / (double) mc.scaledResolution.getScaleFactor(), framebuffer.framebufferTextureHeight / (double) mc.scaledResolution.getScaleFactor());

//                        GlStateManager.enableRescaleNormal();
//                        RenderHelper.enableGUIStandardItemLighting();
//
//                        for (int yi = 1;yi <= length;yi++) {
//                            for (int xi = 1;xi <= 9;xi++) {
//                                final ItemStack itemStack = guiChest.inventorySlots.inventorySlots.get(yi * xi - 1).getStack();
//
//                                if (itemStack != null) {
//                                    mc.getRenderItem().renderItemIntoGUI3D(itemStack, x + ((xi - 1) * 18), y + ((yi - 1) * 18));
//                                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, itemStack, x + ((xi - 1) * 18), y + ((yi - 1) * 18), null);
//                                }
//                            }
//                        }
//
//                        RenderHelper.disableStandardItemLighting();
//                        GlStateManager.disableRescaleNormal();

                            glDepthMask(true);
                            glEnable(GL_DEPTH_TEST);
                            glDisable(GL_BLEND);

                            GL11.glPopMatrix();
                        }
                    } else {
                        animatedPos = null;
                        guiChest = null;
                    }
                }
            }
        });
    };

    @EventLink
    private final Listener<ChestRenderEvent> onChestRender = event ->{
        if (silent.getValue() && blockPos != null) {
            final GuiContainer container = event.getScreen();
            if (container instanceof GuiChest) {
                event.setCancelled();
                if (InventoryMove.INSTANCE.isEnabled()) {
                    mc.inGameHasFocus = true;
                    mc.mouseHelper.grabMouseCursor();
                    mc.setIngameFocus();
                }
            }
        }
    };

    @EventLink()
    private final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        /*
        if (!isChest && onlychest.getValueState())
            return;

         */
        if (mode.getValue().getName().equalsIgnoreCase("normal"))
            work();
    };

    private void work() {
        if (mc.currentScreen instanceof GuiChest || mc.currentScreen instanceof GuiFurnace || mc.currentScreen instanceof GuiBrewingStand) {
            final Container container = mc.thePlayer.openContainer;

            if (GUIDetectionComponent.inGUI() || !this.stopwatch.finished(this.nextClick)) {
                return;
            }

            this.lastSteal++;

            if ((timeCheck.getValue() || lastSteal == 1) && onlyBest.getValue()) {
                int helmet = -1;
                int chestPlate = -1;
                int leggings = -1;
                int boots = -1;

                int sword = -1;
                int pickaxe = -1;
                int axe = -1;
                int shovel = -1;
                int bow = -1;

                int INVENTORY_SLOTS = 40;
                for (int i = 0; i < INVENTORY_SLOTS; i++) {
                    final ItemStack stack = ItemUtil.getItemStack(i);

                    if (stack == null)
                        continue;

                    final Item item = stack.getItem();

                    if (!ItemUtil.useful(stack)) {
                        continue;
                    }

                    if (item instanceof ItemArmor) {
                        final ItemArmor armor = (ItemArmor) item;
                        final double reduction = ItemUtil.armorReduction(stack);

                        switch (armor.armorType) {
                            case 0:
                                if (helmet == -1 || reduction > ItemUtil.armorReduction(ItemUtil.getItemStack(helmet))) {
                                    helmet = i;
                                }
                                break;

                            case 1:
                                if (chestPlate == -1 || reduction > ItemUtil.armorReduction(ItemUtil.getItemStack(chestPlate))) {
                                    chestPlate = i;
                                }
                                break;

                            case 2:
                                if (leggings == -1 || reduction > ItemUtil.armorReduction(ItemUtil.getItemStack(leggings))) {
                                    leggings = i;
                                }
                                break;

                            case 3:
                                if (boots == -1 || reduction > ItemUtil.armorReduction(ItemUtil.getItemStack(boots))) {
                                    boots = i;
                                }
                                break;
                        }
                    }

                    if (item instanceof ItemSword) {
                        if (sword == -1 || ItemUtil.damage(stack) > ItemUtil.damage(ItemUtil.getItemStack(sword))) {
                            sword = i;
                        }
                    }

                    if (item instanceof ItemPickaxe) {
                        if (pickaxe == -1 || ItemUtil.mineSpeed(stack) > ItemUtil.mineSpeed(ItemUtil.getItemStack(pickaxe))) {
                            pickaxe = i;
                        }
                    }

                    if (item instanceof ItemAxe) {
                        if (axe == -1 || ItemUtil.mineSpeed(stack) > ItemUtil.mineSpeed(ItemUtil.getItemStack(axe))) {
                            axe = i;
                        }
                    }

                    if (item instanceof ItemSpade) {
                        if (shovel == -1 || ItemUtil.mineSpeed(stack) > ItemUtil.mineSpeed(ItemUtil.getItemStack(shovel))) {
                            shovel = i;
                        }
                    }

                    if (item instanceof ItemBow) {
                        if (bow == -1 || ItemUtil.bow(stack) > ItemUtil.bow(ItemUtil.getItemStack(bow))) {
                            bow = i;
                        }
                    }
                }

                helmet += 100;
                chestPlate += 100;
                leggings += 100;
                boots += 100;

                sword += 100;
                pickaxe += 100;
                axe += 100;
                shovel += 100;
                bow += 100;

                for (int i = 0; i < container.inventorySlots.size(); i++) {
                    final ItemStack stack = ItemUtil.getItemStack(container, i);
                    if (stack == null || (this.ignoreTrash.getValue() && !ItemUtil.useful(stack)))
                        continue;

                    final Item item = stack.getItem();

                    if (item instanceof ItemArmor) {
                        final ItemArmor armor = (ItemArmor) item;
                        final double reduction = ItemUtil.armorReduction(stack);

                        switch (armor.armorType) {
                            case 0:
                                if (helmet == -1 || helmet == 99 || reduction > ItemUtil.armorReduction(ItemUtil.getItemStack(container, helmet))) {
                                    helmet = i;
                                }
                                break;

                            case 1:
                                if (chestPlate == -1 || chestPlate == 99 || reduction > ItemUtil.armorReduction(ItemUtil.getItemStack(container, chestPlate))) {
                                    chestPlate = i;
                                }
                                break;

                            case 2:
                                if (leggings == -1 || leggings == 99 || reduction > ItemUtil.armorReduction(ItemUtil.getItemStack(container, leggings))) {
                                    leggings = i;
                                }
                                break;

                            case 3:
                                if (boots == -1 || boots == 99 || reduction > ItemUtil.armorReduction(ItemUtil.getItemStack(container, boots))) {
                                    boots = i;
                                }
                                break;
                        }
                    } else if (item instanceof ItemSword) {
                        if (sword == -1 || sword == 99 || ItemUtil.damage(stack) > ItemUtil.damage(ItemUtil.getItemStack(container, sword))) {
                            sword = i;
                        }
                    } else if (item instanceof ItemPickaxe) {
                        if (pickaxe == -1 || pickaxe == 99 || ItemUtil.mineSpeed(stack) > ItemUtil.mineSpeed(ItemUtil.getItemStack(container, pickaxe))) {
                            pickaxe = i;
                        }
                    } else if (item instanceof ItemAxe) {
                        if (axe == -1 || axe == 99 || ItemUtil.mineSpeed(stack) > ItemUtil.mineSpeed(ItemUtil.getItemStack(container, axe))) {
                            axe = i;
                        }
                    } else if (item instanceof ItemSpade) {
                        if (shovel == -1 || shovel == 99 || ItemUtil.mineSpeed(stack) > ItemUtil.mineSpeed(ItemUtil.getItemStack(container, shovel))) {
                            shovel = i;
                        }
                    } else if (item instanceof ItemBow) {
                        if (bow == -1 || bow == 99 || ItemUtil.bow(stack) > ItemUtil.bow(ItemUtil.getItemStack(container, bow))) {
                            bow = i;
                        }
                    } else {
                        shouldTake.add(i);
                    }
                }

                if (helmet != -1 && helmet < 99)
                    shouldTake.add(helmet);
                if (chestPlate != -1 && chestPlate < 99)
                    shouldTake.add(chestPlate);
                if (leggings != -1 && leggings < 99)
                    shouldTake.add(leggings);
                if (boots != -1 && boots < 99)
                    shouldTake.add(boots);
                if (sword != -1 && sword < 99)
                    shouldTake.add(sword);
                if (pickaxe != -1 && pickaxe < 99)
                    shouldTake.add(pickaxe);
                if (axe != -1 && axe < 99)
                    shouldTake.add(axe);
                if (shovel != -1 && shovel < 99)
                    shouldTake.add(shovel);
                if (bow != -1 && bow < 99) {
                    shouldTake.add(bow);
                }
            }

            for (int i = 0; i < container.inventorySlots.size(); i++) {
                final ItemStack stack = ItemUtil.getItemStack(container, i);

                if (stack == null || (this.lastSteal <= 1 && !timeCheck.getValue())) {
                    continue;
                }

                if (this.ignoreTrash.getValue() && !ItemUtil.useful(stack)) {
                    continue;
                }

                if (onlyBest.getValue() && !shouldTake.contains(i)) {
                    continue;
                }

                this.nextClick = Math.round(MathUtil.getRandom(this.delay.getValue().intValue(), this.delay.getSecondValue().intValue()));
                if (sendPacket.getValue()) {
                    mc.playerController.windowClickOnlyPacket(container.windowId, i, 0, 1, mc.thePlayer);
                } else {
                    if (Disabler.INSTANCE.isEnabled() && Disabler.INSTANCE.watchdog.getValue()) {
                        int emptySlot = -1;
                        for (int index = 0; index < mc.thePlayer.inventory.mainInventory.length; index++) {
                            if (mc.thePlayer.inventory.mainInventory[index] == null) {
                                emptySlot = index;
                                break;
                            }
                        }

                        if (emptySlot != -1) {
                            mc.playerController.windowClick(container.windowId, i, 0, 0, mc.thePlayer);
                            if (emptySlot <= 8) {
                                mc.playerController.windowClick(container.windowId, emptySlot + 54, 0, 0, mc.thePlayer);
                            } else {
                                mc.playerController.windowClick(container.windowId, emptySlot + 18, 0, 0, mc.thePlayer);
                            }
                        }
                    } else {
                        mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                    }
                }
                this.stopwatch.reset();
                this.lastClick = 0;
                if (this.nextClick > 0) return;
            }

            this.lastClick++;

            if (this.lastClick > (sendPacket.getValue() ? 5 : 1)) {
                mc.thePlayer.closeScreen();
            }
        } else {
            this.lastClick = 0;
            this.lastSteal = 0;
            shouldTake.clear();
        }
    }

    public Stealer() {
        INSTANCE = this;
        this.setKeyCode(Keyboard.KEY_X);
    }
}