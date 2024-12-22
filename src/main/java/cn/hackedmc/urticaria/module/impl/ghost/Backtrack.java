package cn.hackedmc.urticaria.module.impl.ghost;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.combat.KillAura;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.other.AttackEvent;
import cn.hackedmc.urticaria.newevent.impl.other.WorldChangeEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render3DEvent;
import cn.hackedmc.urticaria.util.render.ColorUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.*;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.LinkedBlockingQueue;

@ModuleInfo(name = "module.ghost.backtrack.name", description = "module.ghost.backtrack.description", category = Category.GHOST)
public class Backtrack extends Module {
    private final NumberValue range = new NumberValue("Max Range", this, 4, 3, 6, 0.1);
    private final BooleanValue velocity = new BooleanValue("Velocity", this, false);
    private final BooleanValue transaction = new BooleanValue("Transaction", this, false);
    private final BooleanValue blockUpdate = new BooleanValue("Block Update", this, false);

    private Entity entity;
    private final Vec3 position = new Vec3(-1, -1, -1);
    private final LinkedBlockingQueue<Packet<?>> delayPacket = new LinkedBlockingQueue<>();
    @Override
    protected void onEnable() {
        release();
    }

    @SuppressWarnings("All")
    private void release() {
        if (mc.getNetHandler() == null) return;

        entity = null;
        while (!delayPacket.isEmpty()) {
            Packet<INetHandlerPlayClient> packet = (Packet<INetHandlerPlayClient>) delayPacket.poll();

            // 处理包前触发 PacketReceiveEvent
            PacketReceiveEvent packetReceiveEvent = new PacketReceiveEvent(packet);
            Client.INSTANCE.getEventBus().handle(packetReceiveEvent);

            if (packetReceiveEvent.isCancelled()) {
                continue;
            }

            // 处理包
            packet.processPacket(mc.getNetHandler());
        }
    }

    @EventLink
    private final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (KillAura.INSTANCE.target != null && entity == null) {
            entity = KillAura.INSTANCE.target;
            position.xCoord = entity.posX;
            position.yCoord = entity.posY;
            position.zCoord = entity.posZ;
        }

        if (entity != null) {
            if (entity.isDead || (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getHealth() <= 0) || !entity.isEntityAlive() || mc.theWorld.getEntityByID(entity.getEntityId()) != entity) {
                entity = null;
                release();
            }
        }
    };

    @EventLink
    private final Listener<WorldChangeEvent> onWorldChange = event -> release();

    @EventLink
    private final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        if (entity == null) return;

        final Packet<?> packet = event.getPacket();

        if (packet instanceof S14PacketEntity) {
            final S14PacketEntity wrapped = (S14PacketEntity) packet;

            if (wrapped.entityId == entity.getEntityId()) {
                position.xCoord += wrapped.getPosX() / 32D;
                position.yCoord += wrapped.getPosY() / 32D;
                position.zCoord += wrapped.getPosZ() / 32D;

                if (mc.thePlayer.getDistanceSq(position.xCoord, position.yCoord, position.zCoord) >= range.getValue().doubleValue()) {
                    entity = null;
                    release();
                } else {
                    event.setCancelled();
                }
            } else {
                event.setCancelled();
            }
        }

        if (packet instanceof S18PacketEntityTeleport) {
            final S18PacketEntityTeleport wrapped = (S18PacketEntityTeleport) packet;

            if (wrapped.entityId == entity.getEntityId()) {
                position.xCoord = wrapped.posX / 32D;
                position.yCoord = wrapped.posY / 32D;
                position.zCoord = wrapped.posZ / 32D;

                if (mc.thePlayer.getDistanceSq(position.xCoord, position.yCoord, position.zCoord) >= range.getValue().doubleValue()) {
                    entity = null;
                    release();
                } else {
                    event.setCancelled();
                }
            } else {
                event.setCancelled();
            }
        }

        if (entity != null && ((velocity.getValue() && packet instanceof S12PacketEntityVelocity) || (transaction.getValue() && (packet instanceof S32PacketConfirmTransaction || packet instanceof S00PacketKeepAlive)) || (blockUpdate.getValue() && (packet instanceof S23PacketBlockChange || packet instanceof S22PacketMultiBlockChange)))) {
            event.setCancelled();

            try {
                delayPacket.put(packet);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    @EventLink
    private final Listener<Render3DEvent> onRender3D = event -> {
        if (entity == null) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GL11.glDepthMask(false);

        double expand = 0.14;
        RenderUtil.color(ColorUtil.withAlpha(getTheme().getFirstColor(), 100));

        RenderUtil.drawBoundingBox(mc.thePlayer.getEntityBoundingBox().offset(-mc.thePlayer.posX, -mc.thePlayer.posY, -mc.thePlayer.posZ).
                offset(position.xCoord, position.yCoord, position.zCoord).expand(expand, expand, expand));

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GL11.glDepthMask(true);
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
        GlStateManager.resetColor();
    };
}
