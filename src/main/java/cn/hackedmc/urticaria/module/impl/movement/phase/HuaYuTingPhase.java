package cn.hackedmc.urticaria.module.impl.movement.phase;

import cn.hackedmc.urticaria.component.impl.player.BlinkComponent;
import cn.hackedmc.urticaria.component.impl.render.notificationcomponent.NotificationComponent;
import cn.hackedmc.urticaria.module.impl.movement.Phase;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.BlockAABBEvent;
import cn.hackedmc.urticaria.newevent.impl.other.WorldChangeEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render3DEvent;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.util.vector.Vector3d;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockGlass;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.awt.*;

public class HuaYuTingPhase extends Mode<Phase> {
    public HuaYuTingPhase(String name, Phase parent) {
        super(name, parent);
    }
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Normal"))
            .add(new SubMode("Boost"))
            .setDefault("Normal");
    private Vector3d startPlayer;
    private boolean phasing;
    private BlockPos startPos;
    private int boostTick;

    @EventLink
    private final Listener<Render3DEvent> onRender3D = event -> {
        if (startPos != null)
            RenderUtil.drawBlockBox(startPos, getTheme().getAccentColor(new Vector2d(0, 100)), false);
    };

    @Override
    public void onEnable() {
        if (mode.getValue().getName().equalsIgnoreCase("Normal")) {
            startPlayer = new Vector3d(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            final BlockPos basePos = new BlockPos(mc.thePlayer);
            startPos = basePos.down();
            phasing = true;
            BlinkComponent.setExempt(C08PacketPlayerBlockPlacement.class);
            BlinkComponent.blinking = true;
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, startPos, EnumFacing.UP));
        }

        boostTick = 0;
    }

    @EventLink
    private final Listener<WorldChangeEvent> onWorld = event -> {
        phasing = false;
        boostTick = 0;
    };

    @Override
    public void onDisable() {
        if (startPos != null && !(mc.theWorld.getBlockState(startPos).getBlock() instanceof BlockAir)) {
            BlinkComponent.packets.forEach(packet -> {
                if (!(packet instanceof C07PacketPlayerDigging)) {
                    if (packet instanceof C03PacketPlayer) {
                        final C03PacketPlayer wrapped = (C03PacketPlayer) packet;

                        if (wrapped.moving) {
                            wrapped.x = startPlayer.getX();
                            wrapped.y = startPlayer.getY();
                            wrapped.z = startPlayer.getZ();
                        }
                    }

                    mc.getNetHandler().addToSendQueueUnregistered(packet);
                }
            });
            BlinkComponent.packets.clear();

            mc.thePlayer.setPosition(startPlayer.getX(), startPlayer.getY(), startPlayer.getZ());
        }
        BlinkComponent.blinking = false;
        startPos = null;
    }

    @EventLink
    private final Listener<BlockAABBEvent> onBlockAABB = event -> {
        if (mode.getValue().getName().equalsIgnoreCase("Normal") && phasing && event.getBlockPos().equals(startPos))
            event.setBoundingBox(null);
    };

    @EventLink
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mode.getValue().getName().equalsIgnoreCase("Normal")) {
            if (mc.thePlayer.posY + 3.1 < startPos.getY()) {
                phasing = false;

                if (mc.theWorld.getBlockState(startPos).getBlock() instanceof BlockAir) {
                    BlinkComponent.blinking = false;
                    this.getParent().toggle();
                    NotificationComponent.post("Phase", "Operation successful!");
                }
            }
        } else {
            if (!phasing) startPos = new BlockPos(mc.thePlayer).up(2);

            if (mc.theWorld.getBlockState(startPos).getBlock() instanceof BlockGlass) {
                if (!phasing) {
                    phasing = true;
                    BlinkComponent.setExempt(C08PacketPlayerBlockPlacement.class);
                    BlinkComponent.blinking = true;
                    boostTick = 0;
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3, mc.thePlayer.posZ);
                }
                boostTick++;

                if (boostTick == 5) {
                    MoveUtil.strafe(5);
                }

                if (boostTick == 1000) {
                    BlinkComponent.packets.clear();
                    BlinkComponent.blinking = false;
                    mc.thePlayer.sendChatMessage("/hub");
                }
            } else {
                BlinkComponent.blinking = false;
            }
        }
    };
}
