package cn.hackedmc.urticaria.component.impl.render.notificationcomponent;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.Component;
import cn.hackedmc.urticaria.module.impl.render.Interface;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.util.animation.Animation;
import cn.hackedmc.urticaria.util.animation.Easing;
import cn.hackedmc.urticaria.util.font.Font;
import cn.hackedmc.urticaria.util.font.FontManager;
import cn.hackedmc.urticaria.util.render.ColorUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.util.render.Stencil;
import cn.hackedmc.urticaria.util.render.StencilUtil;
import cn.hackedmc.urticaria.util.tuples.Triple;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

@Rise
public class NotificationComponent extends Component {
    private static final ArrayList<Notification> NOTIFICATIONS = new ArrayList<>();
    private static final Font titleFont = FontManager.getNunitoBold(22);
    private static final Font describeFont = FontManager.getNunito(20);
    private static final Font iconFont = FontManager.getIconsThree(40);
    private static final DecimalFormat df = new DecimalFormat("0.0");

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<Render2DEvent> onRender2DEvent = event -> {
        if (Interface.INSTANCE.notifyMode.getValue().getName().equalsIgnoreCase("Off")) {
            NOTIFICATIONS.clear();
            return;
        }

        final int width = event.getScaledResolution().getScaledWidth();
        final int height = event.getScaledResolution().getScaledHeight();

        int offset = Interface.INSTANCE.notifyMode.getValue().getName().equalsIgnoreCase("Basic") ? 100 : 50;
        if (NOTIFICATIONS == null || NOTIFICATIONS.isEmpty() || Client.INSTANCE.noNotify) return;
        final Iterator<Notification> iterator = NOTIFICATIONS.iterator();
        while (iterator.hasNext()) {
            final Notification notification = iterator.next();
            if (notification == null) continue;

            final int state = notification.getState();
            final String title = notification.getTitle();
            final String describe = notification.getDescribe() + "(" + (state == 0 ? df.format(notification.getTime() / 1000.0) : (state == 2 ? "0.0" : df.format((notification.getTime() - notification.stopWatch.getElapsedTime()) / 1000.0))) + "s)";
            final Notification.Type type = notification.getType();

            switch (Interface.INSTANCE.notifyMode.getValue().getName().toLowerCase()) {
                case "basic": {
                    final int boxLength = describeFont.width(describe) + 40;
                    final double renderX;
                    double targetY = height - offset;

                    switch (state) {
                        case 0: {
                            final Animation animation = notification.getNotificationIn();
                            animation.run(boxLength);

                            renderX = width - animation.getValue();

                            if (animation.isFinished()) {
                                notification.stopWatch.reset();
                                notification.setState(1);
                            }

                            if (notification.getNotificationY() == null) {
                                notification.setNotificationY(new Animation(Easing.EASE_IN_OUT_QUAD, 200));
                                notification.getNotificationY().setValue(targetY);
                            }

                            break;
                        }

                        default:
                        case 1: {
                            renderX = width - boxLength;

                            if (notification.stopWatch.finished(notification.getTime())) {
                                notification.setState(2);
                            }

                            break;
                        }

                        case 2: {
                            final Animation animation = notification.getNotificationOut();
                            animation.run(boxLength);

                            renderX = width - boxLength + animation.getValue();

                            if (animation.isFinished()) {
                                iterator.remove();
                                continue;
                            }

                            break;
                        }
                    }

                    notification.getNotificationY().run(targetY);
                    double renderY = notification.getNotificationY().getValue();

                    NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                        RenderUtil.roundedRectangle(renderX, renderY, boxLength - 5, 30, 7f, new Color(0, 0, 0, 50));
                        titleFont.drawString(title, renderX + 30, renderY + 5, new Color(255, 255, 255, 255).getRGB());
                        describeFont.drawString(describe, renderX + 30, renderY + 20, new Color(233, 233, 233, 233).getRGB());
                        final String icon;
                        final Color color;
                        switch (type) {
                            default:
                            case INFO:
                                icon = "r";
                                color = new Color(155, 155, 155, 200);
                                break;
                            case SUCCESS:
                                icon = "R";
                                color = new Color(100, 255, 50, 200);
                                break;
                            case ERROR:
                                icon = "Q";
                                color = new Color(255, 50, 50, 200);
                                break;
                            case WARNING:
                                icon = "t";
                                color = new Color(255, 255, 55, 200);
                                break;
                        }
                        iconFont.drawString(icon, renderX + 5, renderY + 8.5, color.getRGB());
                    });

                    NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle(renderX, renderY, boxLength - 5, 30, 7f, Color.WHITE));

                    NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle(renderX, renderY, boxLength - 5, 30, 7f, Color.BLACK));

                    offset += 40;

                    break;
                }

                case "central": {
                    final float targetSize = 1.0f;
                    final double showSize;
                    double targetY = height / 2f + offset;

                    switch (state) {
                        case 0: {
                            final Animation animation = notification.getNotificationIn();
                            animation.run(targetSize);

                            showSize = animation.getValue();

                            if (animation.isFinished()) {
                                notification.stopWatch.reset();
                                notification.setState(1);
                            }

                            if (notification.getNotificationY() == null) {
                                notification.setNotificationY(new Animation(Easing.EASE_IN_OUT_QUAD, 200));
                                notification.getNotificationY().setValue(targetY);
                            }

                            break;
                        }

                        default:
                        case 1: {
                            showSize = targetSize;

                            if (notification.stopWatch.finished(notification.getTime())) {
                                notification.setState(2);
                            }

                            break;
                        }

                        case 2: {
                            final Animation animation = notification.getNotificationOut();
                            animation.run(targetSize);

                            showSize = targetSize - animation.getValue();

                            if (animation.isFinished()) {
                                iterator.remove();
                                continue;
                            }

                            break;
                        }
                    }

                    notification.getNotificationY().run(targetY);
                    double boxLength = describeFont.width(describe) + 40;
                    double renderY = notification.getNotificationY().getValue();
                    double renderX = width / 2f - boxLength / 2.0;
                    double halfX = (boxLength - 5) / 2.0;
                    double halfY = 15;

                    final String icon;
                    final Color color;
                    switch (type) {
                        default:
                        case INFO:
                            icon = "r";
                            color = new Color(155, 155, 155, 150);
                            break;
                        case SUCCESS:
                            icon = "R";
                            color = new Color(108, 255, 50, 150);
                            break;
                        case ERROR:
                            icon = "Q";
                            color = new Color(236, 56, 32, 150);
                            break;
                        case WARNING:
                            icon = "t";
                            color = new Color(255, 255, 55, 150);
                            break;
                    }

                    NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(renderX + halfX, renderY + halfY, 0);
                        GlStateManager.scale(showSize, showSize, showSize);
                        GlStateManager.translate(-halfX, -halfY, 0);
                        RenderUtil.roundedRectangle(0, 0, boxLength - 5, 30, 10f, color);
                        titleFont.drawString(title, 30, 5, new Color(255, 255, 255, 255).getRGB());
                        describeFont.drawString(describe, 30, 20, new Color(233, 233, 233, 233).getRGB());
                        iconFont.drawString(icon, 5, 8.5, ColorUtil.withAlpha(color, 255).getRGB());
                        GlStateManager.popMatrix();
                    });

                    NORMAL_BLUR_RUNNABLES.add(() -> {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(renderX + halfX, renderY + halfY, 0);
                        GlStateManager.scale(showSize, showSize, showSize);
                        GlStateManager.translate(-halfX, -halfY, 0);
                        RenderUtil.roundedRectangle(0, 0, boxLength - 5, 30, 10f, color);
                        GlStateManager.popMatrix();
                    });

                    NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(renderX + halfX, renderY + halfY, 0);
                        GlStateManager.scale(showSize, showSize, showSize);
                        GlStateManager.translate(-halfX, -halfY, 0);
                        RenderUtil.roundedRectangle(0, 0, boxLength - 5, 30, 10f, ColorUtil.withAlpha(color, 200));
                        GlStateManager.popMatrix();
                    });

//                    NORMAL_OUTLINE_RUNNABLES.add(() -> {
//                        GlStateManager.pushMatrix();
//                        GlStateManager.translate(renderX + halfX, renderY + halfY, 0);
//                        GlStateManager.scale(showSize, showSize, showSize);
//                        GlStateManager.translate(-halfX, -halfY, 0);
//                        iconFont.drawString(icon, 5, 8.5, Color.BLACK.getRGB());
//                        GlStateManager.popMatrix();
//                    });

                    offset += 40;

                    break;
                }
            }
        }
    };

    public static void post(String title, String description) {
        post(title, description, 1000);
    }

    public static void post(String title, String description, long time) {
        post(title, description, Notification.Type.INFO, time);
    }

    public static void post(String title, String description, Notification.Type type, long time) {
        NOTIFICATIONS.add(new Notification(title, description, type, time));
    }
}
