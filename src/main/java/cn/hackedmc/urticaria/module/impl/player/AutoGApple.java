package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.component.impl.render.notificationcomponent.NotificationComponent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.player.antivoid.FreezeAntiVoid;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.input.MoveInputEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PostMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.SlowDownEvent;
import cn.hackedmc.urticaria.newevent.impl.other.MoveEvent;
import cn.hackedmc.urticaria.newevent.impl.other.MoveMathEvent;
import cn.hackedmc.urticaria.newevent.impl.other.TickEvent;
import cn.hackedmc.urticaria.newevent.impl.other.WorldChangeEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.util.animation.Animation;
import cn.hackedmc.urticaria.util.animation.Easing;
import cn.hackedmc.urticaria.util.font.Font;
import cn.hackedmc.urticaria.util.font.FontManager;
import cn.hackedmc.urticaria.util.localization.Localization;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import net.minecraft.command.server.CommandMessage;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.viamcp.ViaMCP;
import util.time.StopWatch;

import java.awt.*;
import java.util.concurrent.LinkedBlockingQueue;

@ModuleInfo(name = "module.player.autogapple.name", description = "module.player.autogapple,description", category = Category.PLAYER)
public class AutoGApple extends Module {
    public static AutoGApple INSTANCE;
    public AutoGApple() {
        INSTANCE = this;
    }
    private final NumberValue delay = new NumberValue("Delay", this, 1000, 0, 10000, 100);
    private final NumberValue health = new NumberValue("Health", this, 15, 0, 20, 0.5);
    private final BooleanValue info = new BooleanValue("Eating Info", this, false);
    private final BooleanValue blur = new BooleanValue("Blur", this, true, () -> !info.getValue());
    private final BooleanValue bloom = new BooleanValue("Bloom", this, true, () -> !info.getValue());
    private final BooleanValue noMove = new BooleanValue("Stop move when eating", this, false);
    public final BooleanValue alwaysAttack = new BooleanValue("Attack All Times", this, false);
    private final BooleanValue autoClose = new BooleanValue("Close when no golden apple", this, true);
    private final BooleanValue lagValue = new BooleanValue("Lag when in air", this, false);
    private final BooleanValue notification = new BooleanValue("Notification", this, false);

    private final Animation animation = new Animation(Easing.LINEAR, 50);
    private final Font NORMAL20 = FontManager.getProductSansRegular(24);
    private final StopWatch stopWatch = new StopWatch();
    public static boolean eating = false;
    private int movingPackets = 0;
    private int slot = 0;
    private final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();
    private boolean needSkip = false;

    @Override
    protected void onEnable() {
        packets.clear();
        slot = -1;
        needSkip = false;
        movingPackets = 0;
        eating = false;
        animation.reset();
    }

    @Override
    protected void onDisable() {
        eating = false;
        release();
    }
    @EventLink
    private final Listener<WorldChangeEvent> onWorld = event -> {
        eating = false;
        release();
    };

    @EventLink
    private final Listener<MoveMathEvent> onMoveMath = event -> {
        if (eating && lagValue.getValue() && mc.thePlayer.positionUpdateTicks < 20 && !needSkip) event.setCancelled();
        else if (needSkip) needSkip = false;
    };

    @EventLink(value = Priorities.VERY_LOW)
    private final Listener<PostMotionEvent> onPostMotion = event -> {
        if (eating) {
            movingPackets++;
            packets.add(new C01PacketChatMessage("release"));
        }
    };

    @EventLink(value = Priorities.VERY_LOW)
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mc.thePlayer == null || !mc.thePlayer.isEntityAlive()) {
            eating = false;
            packets.clear();

            return;
        }

        if (!mc.playerController.getCurrentGameType().isSurvivalOrAdventure() || FreezeAntiVoid.running || !stopWatch.finished(delay.getValue().intValue())) {
            eating = false;
            release();

            return;
        }

        slot = getGApple();

        if (slot == -1 || mc.thePlayer.getHealth() >= health.getValue().floatValue()) {
            if (eating) {
                eating = false;
                release();
            }
        } else {
            eating = true;
            if (movingPackets >= 32) {
                if (slot != mc.thePlayer.inventory.currentItem) mc.getNetHandler().addToSendQueueUnregistered(new C09PacketHeldItemChange(slot));
//                mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getStackInSlot(slot));
                mc.getNetHandler().addToSendQueueUnregistered(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(slot)));
                mc.thePlayer.itemInUseCount -= 32;
                release();
                if (slot != mc.thePlayer.inventory.currentItem) mc.getNetHandler().addToSendQueueUnregistered(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                stopWatch.reset();
                animation.reset();

                if (notification.getValue()) {
                    NotificationComponent.post(Localization.get(this.getDisplayName()), Localization.get("notification.eaten"));
                }
            } else if (mc.thePlayer.ticksExisted % 3 == 0) {
                while (!packets.isEmpty()) {
                    final Packet<?> packet = packets.poll();

                    if (packet instanceof C01PacketChatMessage) {
                        break;
                    }

                    if (packet instanceof C03PacketPlayer) {
                        movingPackets--;
                    }

                    mc.getNetHandler().addToSendQueueUnregistered(packet);
                    if (packet instanceof C08PacketPlayerBlockPlacement) {
                        if (ViaMCP.getInstance().getVersion() > 47) {
                            PacketWrapper useItem = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                            useItem.write(Type.VAR_INT, 1);
                            PacketUtil.sendToServer(useItem, Protocol1_8To1_9.class, true, true);
                        }
                    }
                }
            }
        }
    };

    @EventLink
    private final Listener<Render2DEvent> onRender2D = event -> {
        if (eating && info.getValue()) {
            final float step = Math.min(32, movingPackets);

            final int midWidth = event.getScaledResolution().getScaledWidth() / 2;
            final int x = midWidth - 75;
            final int y = event.getScaledResolution().getScaledHeight() / 2 + 40;

            animation.run(140f / 32.0 * step);

            NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                RenderUtil.roundedRectangle(x, y, 150, 40, 3f, new Color(0, 0, 0, 60));
                NORMAL20.drawCenteredString("Eating Time", midWidth, y + 10, new Color(233, 233, 233, 233).getRGB());
                RenderUtil.roundedRectangle(x + 5, y + 24, 140, 6, 3f, new Color(0, 0, 0, 100));
                RenderUtil.drawRoundedGradientRect(x + 5, y + 24, animation.getValue(), 6f, 3f, getTheme().getFirstColor(), getTheme().getSecondColor(), false);
            });

            if (blur.getValue()) {
                NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangleDarker(x, y, 150, 40, 3f, Color.WHITE));
            }

            if (bloom.getValue()) {
                NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle(x, y, 150, 40, 3f, Color.BLACK));
            }
        } else {
            animation.reset();
        }
    };

    @EventLink
    private final Listener<PacketSendEvent> onPacketSend = event -> {
        if (mc.thePlayer == null || !mc.playerController.getCurrentGameType().isSurvivalOrAdventure() || FreezeAntiVoid.running) return;

        final Packet<?> packet = event.getPacket();

        if (packet instanceof C00Handshake || packet instanceof C00PacketLoginStart ||
                packet instanceof C00PacketServerQuery || packet instanceof C01PacketPing ||
                packet instanceof C01PacketEncryptionResponse || packet instanceof C01PacketChatMessage) {
            return;
        }

        if (!(packet instanceof C09PacketHeldItemChange) &&
        !(packet instanceof C0EPacketClickWindow) &&
        !(packet instanceof C16PacketClientStatus) &&
        !(packet instanceof C0DPacketCloseWindow)) {
            if (eating) {
                event.setCancelled();

                packets.add(packet);
            }
        }
    };

    @EventLink()
    private final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity wrapped = (S12PacketEntityVelocity) packet;

            if (wrapped.getEntityID() == mc.thePlayer.getEntityId())
                needSkip = true;
        }
    };

    @EventLink(Priorities.VERY_LOW)
    private final Listener<SlowDownEvent> onSlowDown = event -> {
        if (eating) {
            event.setCancelled(false);
            event.setStrafeMultiplier(0.2f);
            event.setForwardMultiplier(0.2f);
        }
    };

    @EventLink
    private final Listener<MoveInputEvent> onMoveInput = event -> {
        if (eating && noMove.getValue()) {
            event.setForward(0);
            event.setStrafe(0);
        }
    };

    private void release() {
        if (mc.getNetHandler() == null) return;

        while (!packets.isEmpty()) {
            final Packet<?> packet = packets.poll();

            if (packet instanceof C01PacketChatMessage || packet instanceof C08PacketPlayerBlockPlacement || packet instanceof C07PacketPlayerDigging)
                continue;

            mc.getNetHandler().addToSendQueueUnregistered(packet);
        }

        movingPackets = 0;
    }

    private int getGApple() {
        for (int i = 0;i < 9;i++) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

            if (stack == null)
                continue;

            if (stack.getItem() instanceof ItemAppleGold) {
                return i;
            }
        }

        if (autoClose.getValue())
            this.toggle();

        return -1;
    }
}
