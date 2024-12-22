package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.util.PotionData;
import cn.hackedmc.urticaria.util.font.Font;
import cn.hackedmc.urticaria.util.font.FontManager;
import cn.hackedmc.urticaria.util.render.Animator;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.value.impl.*;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@ModuleInfo(name = "module.render.effects.name", description = "module.render.effects.description", category = Category.RENDER)
public class Effects extends Module {
    private final Font productSansMedium22 = FontManager.getProductSansBold(24);
    private final Font productSansMedium18 = FontManager.getProductSansRegular(22);
    private final DragValue position = new DragValue("", this, new Vector2d(200, 200));
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Modern"))
            .add(new SubMode("Urticaria"))
            .setDefault("Modern");
    private final BooleanValue blur = new BooleanValue("Blur", this, false);
    private final BooleanValue bloom = new BooleanValue("Bloom", this, false);
    private final NumberValue alpha = new NumberValue("Alpha", this, 50, 0, 255, 1);
    private final NumberValue size = new NumberValue("Size", this, 2.5f, 1f, 10f, 0.5f, () -> !mode.getValue().getName().equalsIgnoreCase("urticaria"));
    private final NumberValue radius = new NumberValue("Radius", this, 1f, 1f, 5f, 1f, () -> !mode.getValue().getName().equalsIgnoreCase("urticaria"));
    private final BooleanValue background = new BooleanValue("Background", this, false);

    private int yWith;
    private final Map<Potion, PotionData> potionMap = new HashMap<>();
    private final Map<Integer, Integer> potionMaxDurations = new HashMap<>();

    private final int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private final String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    private String intToRomanByGreedy(int num) {
        int n = num;
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        while (i < values.length && n >= 0) {
            while (values[i] <= n) {
                n -= values[i];
                stringBuilder.append(symbols[i]);
            }
            i++;
        }
        return stringBuilder.toString();
    }

    @EventLink
    private final Listener<Render2DEvent> onRender2D = event -> {
        if (mc.thePlayer == null) return;

        if (!mc.thePlayer.getActivePotionEffects().isEmpty() || mc.currentScreen instanceof GuiChat) {
            final float baseX = (float) position.position.x;
            final float baseY = (float) position.position.y;
            final Color color1 = getTheme().getFirstColor();
            final Color color2 = getTheme().getSecondColor();

            switch (mode.getValue().getName().toLowerCase()) {
                case "modern": {
                    position.scale = new Vector2d(100f, 39 + yWith);

                    break;
                }
                case "urticaria": {
                    if (bloom.getValue())
                        NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                            RenderUtil.roundedRectangleDarker(baseX, baseY + 1.5f, 109.5f, 52.5 + yWith + size.getValue().floatValue(), 0, new Color(0, 0, 0, 125));
                            RenderUtil.roundedRectangleDarker(baseX, baseY, 109.5f, 1f, 0f, new Color(0, 0, 0, 125));
                        });

                    if (blur.getValue())
                        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangleDarker(baseX, baseY + 1.5f, 109.5f, 52.5 + yWith + size.getValue().floatValue(), 0, Color.WHITE));

                    NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                        position.scale = new Vector2d(109.5f, 59 + yWith);
                        RenderUtil.roundedRectangleDarker(baseX, baseY + 1.5f, 109.5f, 52.5 + yWith + size.getValue().floatValue(), 0, new Color(0, 0, 0, alpha.getValue().intValue()));
                        productSansMedium22.drawString("Potions", baseX + 2.5f, baseY + 6.5, getTheme().getAccentColor(new Vector2d(0, 100)).getRGB());
                        RenderUtil.drawRoundedGradientRect(baseX, baseY, 109.5f, 1f, 0f, color1, color2, false);
                    });

                    break;
                }
            }

            int y = 0;
            for (PotionEffect potionEffect : mc.thePlayer.getActivePotionEffects()) {
                final Potion potion = Potion.potionTypes[potionEffect.potionID];
                final String name = I18n.format(potion.getName());
                final PotionData potionData;
                PotionData dataTmp = potionMap.get(potion);
                if (dataTmp == null) {
                    potionMap.put(potion, (potionData = new PotionData(potion, new Animator(0f, 40f + y, 0, 0, 0.1f), potionEffect.amplifier)));
                } else {
                    potionData = dataTmp;
                }
                if (potionMaxDurations.get(potionEffect.potionID) == null || potionMaxDurations.get(potionEffect.potionID) < potionEffect.duration) {
                    potionMaxDurations.put(potionEffect.potionID, potionEffect.duration);
                }
                boolean flag = true;
                for (PotionEffect checkEffect : mc.thePlayer.getActivePotionEffects()) {
                    if (checkEffect.amplifier == potionData.getLevel()) {
                        flag = false;
                        break;
                    }
                }
                if (flag) potionMap.remove(potion);
                final int potionMaxTime;
                final int potionTime;
                final String potionDuration = Potion.getDurationString(potionEffect);
                if (potionDuration.contains("*")) {
                    potionMaxTime = potionTime = 1;
                } else {
                    String[] potionDurations = potionDuration.split(":");
                    potionMaxTime = Integer.parseInt(potionDurations[1]);
                    potionTime = Integer.parseInt(potionDurations[0]);
                }
                final int lifeTime = (potionTime * 60 + potionMaxTime);
                if (potionData.maxTimer == 0 || lifeTime > potionData.maxTimer) potionData.maxTimer = lifeTime;
                float state = 0;
                if (lifeTime >= 0.0) state = (float) (((double) lifeTime / (potionData.maxTimer)) * 100.0);
                final int position = Math.round(potionData.translate.getPosY() + 5);
                state = Math.max(state, 2.0f);
                potionData.translate.setTargetX(0f);
                potionData.translate.setTargetY(y);
                potionData.translate.update();
                potionData.animationX = 1.2f * state;
                switch (mode.getValue().getName().toLowerCase()) {
                    case "modern": {
                        yWith = Math.round(potionData.translate.getPosY());

                        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                            if (background.getValue()) RenderUtil.roundedRectangle(baseX, baseY + Math.round(potionData.translate.getPosY()), 100, 39, 3, new Color(0, 0, 0, alpha.getValue().intValue()));
                            final float posY = potionData.translate.getPosY() + 17.5f;
                            productSansMedium18.drawString(name + " " + intToRomanByGreedy(potionEffect.amplifier + 1), baseX + 28f, baseY + posY - productSansMedium18.height() + 2, new Color(255, 255, 255, 200).getRGB());
                            productSansMedium18.drawString(Potion.getDurationString(potionEffect), baseX + 28f, baseY + posY + 6f, new Color(255, 255, 255, 125).getRGB());
                            if (potion.hasStatusIcon()) {
                                GlStateManager.pushMatrix();
                                GL11.glDisable(2929);
                                GL11.glEnable(3042);
                                GL11.glDepthMask(false);
                                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                                final int statusIconIndex = potion.getStatusIconIndex();
                                mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                                mc.ingameGUI.drawTexturedModalRect(
                                        baseX + 5f,
                                        baseY + position + 2f,
                                        statusIconIndex % 8 * 18,
                                        198 + statusIconIndex / 8 * 18,
                                        18,
                                        18
                                );
                                GL11.glDepthMask(true);
                                GL11.glDisable(3042);
                                GL11.glEnable(2929);
                                GlStateManager.popMatrix();
                            }
                        });

                        if (bloom.getValue()) NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle(baseX, baseY + Math.round(potionData.translate.getPosY()), 100, 39, 3, Color.BLACK));

                        if (blur.getValue()) NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle(baseX, baseY + Math.round(potionData.translate.getPosY()), 100, 39, 3, Color.WHITE));

                        y += 39;

                        break;
                    }

                    case "urticaria": {
                        yWith = Math.round(potionData.translate.getPosY());

                        if (bloom.getValue())
                            NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                                if (background.getValue()) RenderUtil.roundedRectangle(baseX + 1.5f, baseY + Math.round(potionData.translate.getPosY()) + 17.5, 105, 34.5f + size.getValue().floatValue(), 3, new Color(0, 0, 0, 125));
                                RenderUtil.roundedRectangle(baseX + 3.5f, baseY + Math.round(potionData.translate.getPosY()) + 50, 102, size.getValue().floatValue(), radius.getValue().floatValue(), Color.WHITE);
                            });

                        if (blur.getValue())
                            NORMAL_BLUR_RUNNABLES.add(() -> {
                                if (background.getValue()) RenderUtil.roundedRectangle(baseX + 1.5f, baseY + Math.round(potionData.translate.getPosY()) + 17.5, 105, 34.5f + size.getValue().floatValue(), 3, Color.WHITE);
                                RenderUtil.roundedRectangle(baseX + 3.5f, baseY + Math.round(potionData.translate.getPosY()) + 50, 102, size.getValue().floatValue(), radius.getValue().floatValue(), Color.WHITE);
                            });

                        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                            if (background.getValue()) RenderUtil.roundedRectangle(baseX + 1.5f, baseY + Math.round(potionData.translate.getPosY()) + 17.5, 105, 34.5f + size.getValue().floatValue(), 3, new Color(0, 0, 0, 40));
                            final float potionDurationRatio = (float) potionEffect.duration / (potionMaxDurations.get(potionEffect.potionID));
                            RenderUtil.roundedRectangle(baseX + 3.5f, baseY + Math.round(potionData.translate.getPosY()) + 50, 102, size.getValue().floatValue(), radius.getValue().floatValue(), new Color(0, 0, 0, 70));
                            RenderUtil.drawRoundedGradientRect(baseX + 3.5f, baseY + Math.round(potionData.translate.getPosY()) + 50, potionDurationRatio * 102, size.getValue().floatValue(), radius.getValue().floatValue(), color1, color2, false);
                            final float posY = potionData.translate.getPosY() + 17.5f;
                            productSansMedium18.drawString(name + " " + intToRomanByGreedy(potionEffect.amplifier + 1), baseX + 29.5f, baseY + posY - productSansMedium18.height() + 16.5, new Color(255, 255, 255, 200).getRGB());
                            productSansMedium18.drawString(Potion.getDurationString(potionEffect), baseX + 29.5f, baseY + posY + 20.5f, new Color(255, 255, 255, 125).getRGB());
                            if (potion.hasStatusIcon()) {
                                GlStateManager.pushMatrix();
                                GL11.glDisable(2929);
                                GL11.glEnable(3042);
                                GL11.glDepthMask(false);
                                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                                final int statusIconIndex = potion.getStatusIconIndex();
                                mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                                mc.ingameGUI.drawTexturedModalRect(
                                        baseX + 6.5f,
                                        baseY + position + 16.5f,
                                        statusIconIndex % 8 * 18,
                                        198 + statusIconIndex / 8 * 18,
                                        18,
                                        18
                                );
                                GL11.glDepthMask(true);
                                GL11.glDisable(3042);
                                GL11.glEnable(2929);
                                GlStateManager.popMatrix();
                            }
                        });

                        y += 39 + size.getValue().intValue();

                        break;
                    }
                }
            }
        } else {
            yWith = 0;
        }
    };
}
