package cn.hackedmc.urticaria.module.impl.other;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.player.BlinkComponent;
import cn.hackedmc.urticaria.component.impl.player.PingSpoofComponent;
import cn.hackedmc.urticaria.component.impl.render.ESPComponent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.util.chat.ChatUtil;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.math.MathUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.DragValue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.*;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@Rise
@ModuleInfo(name = "module.other.debugger.name", category = Category.OTHER, description = "module.other.debugger.description")
public final class Debugger extends Module implements InstanceAccess {
    private final BooleanValue placement = new BooleanValue("Place", this, false);
    private final BooleanValue clickWindow = new BooleanValue("Click Window", this, false);
    private final BooleanValue transaction = new BooleanValue("Transactions", this, true);
    private final BooleanValue keepAlive = new BooleanValue("Keep Alive", this, true);
    private final BooleanValue teleport = new BooleanValue("Teleport", this, true);
    private final BooleanValue velocity = new BooleanValue("Velocity", this, true);
    private final BooleanValue abilities = new BooleanValue("Abilities", this, true);
    private final BooleanValue devPanel = new BooleanValue("Dev Panel", this, false);
    private final BooleanValue eventCalls = new BooleanValue("Event Calls", this, false);

    private final DragValue position = new DragValue("", this, new Vector2d(200, 200), true);

    private final DateTimeFormatter date = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public static HashMap<String, Integer> calls = new HashMap<>();
    private long threadLag;
    private boolean measuring;

    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        final Packet<?> packet = event.getPacket();

        if (clickWindow.getValue() && packet instanceof C0EPacketClickWindow) {
            final C0EPacketClickWindow clickWindow = ((C0EPacketClickWindow) packet);

            ChatUtil.display(EnumChatFormatting.YELLOW + "Click Window:" + EnumChatFormatting.RESET + " (Window ID: %s)   (Slot ID: %s)   (Used Button: %s)   (Action Number: %s)   (Mode: %s)", clickWindow.getWindowId(), clickWindow.getSlotId(), clickWindow.getUsedButton(), clickWindow.getActionNumber(), clickWindow.getMode());
        }

        if (placement.getValue() && packet instanceof C08PacketPlayerBlockPlacement) {
            final C08PacketPlayerBlockPlacement wrapped = (C08PacketPlayerBlockPlacement) packet;

            ChatUtil.display(EnumChatFormatting.BLUE + "Placement:" + EnumChatFormatting.RESET + "(Block Pos: %s)   (Placed Block Direction: %s)   (Item Stack: %s)   (Facing X: %s)   (Facing Y: %s)   (Facing Z: %s)", wrapped.getPosition(), wrapped.getPlacedBlockDirection(), wrapped.getStack(), wrapped.facingX, wrapped.facingY, wrapped.facingZ);
        }
    };

    @EventLink()
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {

        final Packet<?> packet = event.getPacket();

        if (transaction.getValue() && packet instanceof S32PacketConfirmTransaction) {
            final S32PacketConfirmTransaction transaction = ((S32PacketConfirmTransaction) packet);

            ChatUtil.display(EnumChatFormatting.RED + " Transaction " + EnumChatFormatting.RESET + " (ID: %s)   (WindowID: %s)", transaction.actionNumber, transaction.windowId);
        } else if (keepAlive.getValue() && packet instanceof S00PacketKeepAlive) {
            final S00PacketKeepAlive wrapper = ((S00PacketKeepAlive) packet);

            ChatUtil.display(EnumChatFormatting.GREEN + " Keep Alive " + EnumChatFormatting.RESET + " (ID: %s)", wrapper.func_149134_c());
        } else if (teleport.getValue() && packet instanceof S08PacketPlayerPosLook) {
            final S08PacketPlayerPosLook wrapper = ((S08PacketPlayerPosLook) packet);

            ChatUtil.display(EnumChatFormatting.BLUE + " Server Teleport " + EnumChatFormatting.RESET + " (Position: %s)",
                    MathUtil.round(wrapper.x, 3) + " " +
                            MathUtil.round(wrapper.y, 3) + " " +
                            MathUtil.round(wrapper.z, 3));
        } else if (velocity.getValue() && packet instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity wrapper = ((S12PacketEntityVelocity) packet);

            if (wrapper.getEntityID() == mc.thePlayer.getEntityId()) {
                ChatUtil.display(EnumChatFormatting.LIGHT_PURPLE + " Velocity " + EnumChatFormatting.RESET + " (DeltaX: %s) (DeltaY: %s)  (DeltaZ: %s) ",
                        wrapper.motionX / 8000D, wrapper.motionY / 8000D, wrapper.motionZ / 8000D);
            }
        } else if (velocity.getValue() && packet instanceof S27PacketExplosion) {
            ChatUtil.display(EnumChatFormatting.LIGHT_PURPLE + " Explosion (Velocity) ");
        } else if (abilities.getValue() && packet instanceof S39PacketPlayerAbilities) {
            ChatUtil.display(EnumChatFormatting.YELLOW + " Abilities");
        }
    };

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {

        if (devPanel.getValue()) {
            double padding = 10;
            position.scale = new Vector2d(180, 207);

            NORMAL_POST_RENDER_RUNNABLES.add(() -> RenderUtil.roundedRectangle(position.position.x, position.position.y, position.scale.x, position.scale.y, getTheme().getRound(), getTheme().getBackgroundShade()));
            NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle(position.position.x, position.position.y, position.scale.x, position.scale.y, getTheme().getRound(), Color.BLACK));
            NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle(position.position.x, position.position.y, position.scale.x, position.scale.y, getTheme().getRound(), getTheme().getDropShadow()));

            NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                mc.fontRendererObj.drawStringWithShadow(Client.NAME + " " + Client.VERSION + " INDEV " + date.format(LocalDateTime.now()), position.position.x + padding, position.position.y + padding, new Color(255, 255, 0).getRGB());
                mc.fontRendererObj.drawStringWithShadow("FPS: " + Minecraft.getDebugFPS() + " [target " + mc.getLimitFramerate() + "]", position.position.x + padding, position.position.y + padding * 2, new Color(255, 255, 0).getRGB());

                mc.fontRendererObj.drawString("Debugger", position.position.x + padding, position.position.y + padding * 4, getTheme().getFirstColor().hashCode());

                if (Client.DEVELOPMENT_SWITCH) {
                    mc.fontRendererObj.drawString("PingSpoof: " + PingSpoofComponent.spoofing + " Amount: " + PingSpoofComponent.delay, position.position.x + padding, position.position.y + padding * 5, Color.WHITE.hashCode());
                    mc.fontRendererObj.drawString("Blink: " + BlinkComponent.blinking, position.position.x + padding, position.position.y + padding * 6, Color.WHITE.hashCode());
                } else {
                    mc.fontRendererObj.drawString("Hidden due to not in dev mode", position.position.x + padding, position.position.y + padding * 5, Color.WHITE.hashCode());
                    mc.fontRendererObj.drawString("Hidden due to not in dev mode", position.position.x + padding, position.position.y + padding * 6, Color.WHITE.hashCode());
                }

                mc.fontRendererObj.drawString("Bot Amount: " + Client.INSTANCE.getBotManager().size(), position.position.x + padding, position.position.y + padding * 7, Color.WHITE.hashCode());
                mc.fontRendererObj.drawString("ESPs Amount: " + ESPComponent.esps.size(), position.position.x + padding, position.position.y + padding * 8, Color.WHITE.hashCode());

                mc.fontRendererObj.drawString("Performance", position.position.x + padding, position.position.y + padding * 9, getTheme().getFirstColor().hashCode());
                mc.fontRendererObj.drawString("Bloom: " + bloomProfiler.getLastTotalTime() + " ns", position.position.x + padding, position.position.y + padding * 10, Color.WHITE.hashCode());
                mc.fontRendererObj.drawString("Render2D: " + render2dProfiler.getLastTotalTime() + " ns", position.position.x + padding, position.position.y + padding * 11, Color.WHITE.hashCode());
                mc.fontRendererObj.drawString("Render Limited 2D: " + renderLimited2dProfiler.getLastTotalTime() + " ns", position.position.x + padding, position.position.y + padding * 12, Color.WHITE.hashCode());
                mc.fontRendererObj.drawString("OutLine: " + outlineProfiler.getLastTotalTime() + " ns", position.position.x + padding, position.position.y + padding * 13, Color.WHITE.hashCode());
                mc.fontRendererObj.drawString("Blur: " + blurProfiler.getLastTotalTime() + " ns", position.position.x + padding, position.position.y + padding * 14, Color.WHITE.hashCode());
                mc.fontRendererObj.drawString("Drag: " + dragProfiler.getLastTotalTime() + " ns", position.position.x + padding, position.position.y + padding * 15, Color.WHITE.hashCode());
                mc.fontRendererObj.drawString("ThreadLag: " + threadLag, position.position.x + padding, position.position.y + padding * 16, Color.WHITE.hashCode());

                mc.fontRendererObj.drawString("Other", position.position.x + padding, position.position.y + padding * 18, getTheme().getFirstColor().hashCode());
                mc.fontRendererObj.drawString("Timer: " + mc.timer.timerSpeed, position.position.x + padding, position.position.y + padding * 19, Color.WHITE.hashCode());
            });
        }
    };

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (measuring) return;

        long systemTime = System.currentTimeMillis();
        measuring = true;

        boolean run = mc.thePlayer.ticksExisted % 100 == 0 && eventCalls.getValue();
        threadPool.execute(() -> {
            threadLag = System.currentTimeMillis() - systemTime;
            measuring = false;

            if (run) {
                ChatUtil.display("Displaying Calls: ");

                for (String name : calls.keySet()) {
                    ChatUtil.display(name + ": " + calls.get(name));
                }

                calls.clear();
            }
        });
    };
}
