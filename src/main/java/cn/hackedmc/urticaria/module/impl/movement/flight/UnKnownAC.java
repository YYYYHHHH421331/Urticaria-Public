package cn.hackedmc.urticaria.module.impl.movement.flight;

import cn.hackedmc.urticaria.component.impl.player.GUIDetectionComponent;
import cn.hackedmc.urticaria.component.impl.player.RotationComponent;
import cn.hackedmc.urticaria.component.impl.player.SlotComponent;
import cn.hackedmc.urticaria.module.impl.movement.Flight;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.input.MoveInputEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.other.BlockAABBEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.util.player.SlotUtil;
import cn.hackedmc.urticaria.util.vector.Vector2f;
import cn.hackedmc.urticaria.component.impl.player.rotationcomponent.MovementFix;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import net.minecraft.block.BlockAir;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;

/**
 * @author Alan
 * @since 19.08.2022
 */

public class UnKnownAC extends Mode<Flight> {
    private NumberValue speed = new NumberValue("Speed", this, 2, 1, 7, 0.25);
    private double y;
    private int ticks;
    private C08PacketPlayerBlockPlacement placement;

    public UnKnownAC(String name, Flight parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        y = Math.floor(mc.thePlayer.posY);
        placement = null;
        ticks = 0;
    }

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {

        final int slot = SlotUtil.findBlock();

        if (slot == -1) {
            return;
        }

        SlotComponent.setSlot(slot, false);

        if (placement != null) {
            return;
        }

        MoveUtil.stop();

        RotationComponent.setRotations(new Vector2f(mc.thePlayer.rotationYaw, 90), 10, MovementFix.TRADITIONAL);

        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
        }

        if (RotationComponent.rotations.y >= 89 &&
                mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
                mc.thePlayer.posY > mc.objectMouseOver.getBlockPos().add(0, 2, 0).getY()) {

            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, SlotComponent.getItemStack(),
                    mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec);

            PacketUtil.send(new C0APacketAnimation());
        }
    };

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        final int slot = SlotUtil.findBlock();

        if (slot == -1) {
            return;
        }

        mc.timer.timerSpeed = Math.max(1, speed.getValue().floatValue() - ticks / 50f);

        if (placement == null) {
            return;
        }

        ticks++;

        if (ticks == 25) {
            PlayerUtil.fakeDamage();
        }

        if (mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindSneak.isKeyDown()) {
            y = mc.thePlayer.posY;
        }

        if (!GUIDetectionComponent.inGUI() && Math.random() > 0.7) {
            PacketUtil.send(placement);
        }

        if (mc.thePlayer.onGround) {
            if (!GUIDetectionComponent.inGUI()) {
                PacketUtil.send(placement);
            }

            mc.thePlayer.jump();
        }
    };


    @EventLink()
    public final Listener<BlockAABBEvent> onBlockAABB = event -> {
        if (placement == null) {
            return;
        }

        if (event.getBlock() instanceof BlockAir && !mc.gameSettings.keyBindSneak.isKeyDown() && (mc.thePlayer.posY < y + 1 || mc.gameSettings.keyBindJump.isKeyDown())) {
            final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();

            if (y < mc.thePlayer.posY) {
                event.setBoundingBox(AxisAlignedBB.fromBounds(
                        -15,
                        -1,
                        -15,
                        15,
                        1,
                        15
                ).offset(x, y, z));
            }
        }
    };


    @EventLink()
    public final Listener<MoveInputEvent> onMove = event -> {
        event.setSneak(false);
    };

    @EventLink()
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        Packet<?> packet = event.getPacket();

        if (packet instanceof C08PacketPlayerBlockPlacement) {
            placement = (C08PacketPlayerBlockPlacement) packet;
        }
    };
}