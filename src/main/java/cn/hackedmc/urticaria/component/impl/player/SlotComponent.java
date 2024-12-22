package cn.hackedmc.urticaria.component.impl.player;

import cn.hackedmc.urticaria.component.Component;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.inventory.SyncCurrentItemEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.util.animation.Animation;
import cn.hackedmc.urticaria.util.animation.Easing;
import cn.hackedmc.urticaria.util.font.Font;
import cn.hackedmc.urticaria.util.font.FontManager;
import cn.hackedmc.urticaria.util.render.ColorUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.util.render.Stencil;
import cn.hackedmc.urticaria.util.render.StencilUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import java.awt.*;

public final class SlotComponent extends Component {
    private ItemStack lastStack = null;

    private static final Animation animation = new Animation(Easing.EASE_IN_OUT_CUBIC, 250);
    private static boolean render;

    public static void setSlotNative(final int slot, final boolean render) {
        if (slot < 0 || slot > 8) {
            return;
        }

        mc.thePlayer.inventory.alternativeCurrentItem = slot;
        mc.thePlayer.inventory.alternativeSlot = true;
        mc.thePlayer.inventory.breakNotNative = true;
        SlotComponent.render = render;
    }

    public static void setSlot(final int slot, final boolean render) {
        if (slot < 0 || slot > 8) {
            return;
        }

        mc.thePlayer.inventory.alternativeCurrentItem = slot;
        mc.thePlayer.inventory.alternativeSlot = true;
        mc.thePlayer.inventory.breakNotNative = false;
        SlotComponent.render = render;
    }

    public static void setSlot(final int slot) {
        setSlot(slot, true);
    }

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        final ScaledResolution scaledResolution = event.getScaledResolution();
        final float x = scaledResolution.getScaledWidth() / 2f - 10;
        final float y = scaledResolution.getScaledHeight() / 2f + 20;
        final double destinationY = render && mc.thePlayer.inventory.alternativeSlot &&
                (mc.thePlayer.inventory.alternativeCurrentItem != mc.thePlayer.inventory.currentItem || getItemStack() == null || getItemStack().getItem() instanceof ItemBlock) &&
                mc.currentScreen == null ? 20 : 0;
        animation.run(destinationY);
        animation.setDuration(250);

        if (!render || (animation.isFinished() && !mc.thePlayer.inventory.alternativeSlot)) {
            return;
        }

        if (mc.thePlayer.inventory.alternativeSlot)
            lastStack = mc.thePlayer.inventoryContainer.getSlot(mc.thePlayer.inventory.alternativeCurrentItem + 36).getStack();
        final ItemStack itemStack = lastStack;
        if (itemStack != null) {
            final Runnable runnable = () -> {
                RenderUtil.rectangle(x, y, 20, animation.getValue(), Color.BLACK);
            };

            NORMAL_BLUR_RUNNABLES.add(runnable);
            NORMAL_POST_BLOOM_RUNNABLES.add(runnable);

            NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                final float itemX = x + 2f;
                final float itemY = y + 3.5f;

                GlStateManager.enableRescaleNormal();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                RenderHelper.enableGUIStandardItemLighting();

                final RenderItem itemRenderer = mc.getRenderItem();

                GlStateManager.pushMatrix();
//                final float f = (float) itemStack.animationsToGo - event.getPartialTicks();
//
//                if (f > 0.0F) {
//                    GlStateManager.pushMatrix();
//                    final float f1 = 1.0F + f / 5.0F;
//                    GlStateManager.translate((float) (itemX + 8), (float) (itemY + 12), 0.0F);
//                    GlStateManager.scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
//                    GlStateManager.translate((float) (-(itemX + 8)), (float) (-(itemY + 12)), 0.0F);
//                }

                Stencil.write(false);
                if (!animation.isFinished()) RenderUtil.rectangle(x, y + animation.getValue(), 20, 20 - animation.getValue());
                Stencil.erase(false);
                StencilUtil.bindReadStencilBuffer(0);
                RenderUtil.drawRoundedGradientRect(x, y, 20, 2, 0, getTheme().getFirstColor(), getTheme().getSecondColor(), false);
                RenderUtil.rectangle(x, y + 2, 20, 18, new Color(0, 0, 0, 50));
                itemRenderer.renderItemAndEffectIntoGUI(itemStack, itemX, itemY);
                itemRenderer.renderItemOverlays(mc.fontRendererObj, itemStack, (int) itemX, (int) itemY);
                Stencil.dispose();

//                if (f > 0.0F) {
//                    GlStateManager.popMatrix();
//                }
                GlStateManager.popMatrix();

                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableRescaleNormal();
                GlStateManager.disableBlend();
            });
        }
    };

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<SyncCurrentItemEvent> onSyncItem = event -> {
        final InventoryPlayer inventoryPlayer = mc.thePlayer.inventory;

        event.setSlot(inventoryPlayer.alternativeSlot && !inventoryPlayer.breakNotNative ? inventoryPlayer.alternativeCurrentItem : inventoryPlayer.currentItem);
    };

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        mc.thePlayer.inventory.alternativeSlot = false;
    };

    public static ItemStack getItemStack() {
        return (mc.thePlayer == null || mc.thePlayer.inventoryContainer == null ? null : mc.thePlayer.inventoryContainer.getSlot(getItemIndex() + 36).getStack());
    }

    public static ItemStack getItemStackNative() {
        return (mc.thePlayer == null || mc.thePlayer.inventoryContainer == null ? null : mc.thePlayer.inventoryContainer.getSlot(getItemIndexNative() + 36).getStack());
    }

    public static int getItemIndexNative() {
        final InventoryPlayer inventoryPlayer = mc.thePlayer.inventory;
        return inventoryPlayer.alternativeSlot ? inventoryPlayer.alternativeCurrentItem : inventoryPlayer.currentItem;
    }

    public static int getItemIndex() {
        final InventoryPlayer inventoryPlayer = mc.thePlayer.inventory;
        return inventoryPlayer.alternativeSlot && !inventoryPlayer.breakNotNative ? inventoryPlayer.alternativeCurrentItem : inventoryPlayer.currentItem;
    }
}