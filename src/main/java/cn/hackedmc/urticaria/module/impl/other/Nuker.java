package cn.hackedmc.urticaria.module.impl.other;

import cn.hackedmc.urticaria.component.impl.player.GUIDetectionComponent;
import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.player.RotationComponent;
import cn.hackedmc.urticaria.component.impl.player.rotationcomponent.MovementFix;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.input.MovingObjectEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.other.WorldChangeEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.newevent.impl.render.LookEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render3DEvent;
import cn.hackedmc.urticaria.util.RayCastUtil;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.pathfinding.unlegit.MainPathFinder;
import cn.hackedmc.urticaria.util.pathfinding.unlegit.Vec3;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.util.render.ColorUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.util.rotation.RotationUtil;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.util.vector.Vector2f;
import cn.hackedmc.urticaria.util.vector.Vector3d;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBed;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.*;
import util.time.StopWatch;

import java.util.*;

@Rise
@ModuleInfo(name = "module.other.nuker.name", description = "module.other.nuker.description", category = Category.OTHER)
public final class Nuker extends Module {
    public static Nuker INSTANCE;

    public Nuker() {
        INSTANCE = this;
    }

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Nearby"))
            .add(new SubMode("Teleport"))
            .add(new SubMode("Bed"))
            .add(new SubMode("Watchdog"))
            .setDefault("Nearby");

    private final NumberValue delay = new NumberValue("Delay", this, 150, 0, 5000, 50);
    private final NumberValue range = new NumberValue("Range", this, 4, 1, 6, 0.5);
    private final BooleanValue rotations = new BooleanValue("Rotations", this, false);
    private final BooleanValue scatter = new BooleanValue("Scatter", this, false);
    private final BooleanValue swing = new BooleanValue("Swing", this, false);
    private final BooleanValue mark = new BooleanValue("Mark", this, false);

    private final List<Vec3i> vec3List = Arrays.asList(
            new Vec3i(1, 0, 0),
            new Vec3i(-1, 0, 0),
            new Vec3i(0, 1, 0),
            new Vec3i(0, 0, 1),
            new Vec3i(0, 0, -1)
    );
    private BlockPos renderBlock;
    public boolean needUpdate = false;
    private final StopWatch stopWatch = new StopWatch();

    @Override
    protected void onEnable() {
        needUpdate = false;
        renderBlock = null;
    }

    @Override
    protected void onDisable() {
        if (needUpdate) {
            mc.gameSettings.keyBindAttack.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindAttack));
            needUpdate = false;
        }
    }

    @EventLink
    private final Listener<Render3DEvent> onRender3D = event -> {
        if ((mode.getValue().getName().equalsIgnoreCase("bed") || mode.getValue().getName().equalsIgnoreCase("watchdog")) && mark.getValue() && renderBlock != null)
            RenderUtil.drawBlockBox(renderBlock, ColorUtil.withAlpha(getTheme().getAccentColor(new Vector2d(0, 100)), 150), false);
    };

    @EventLink
    public final Listener<MovingObjectEvent> onLook = event -> {
        if (mode.getValue().getName().equalsIgnoreCase("Watchdog")) {
            if (renderBlock != null) {
                event.movingObjectPosition = new MovingObjectPosition(new net.minecraft.util.Vec3(0, 0, 0), EnumFacing.UP, renderBlock);
            }
        }
    };

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (needUpdate) {
            mc.gameSettings.keyBindAttack.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindAttack));
            needUpdate = false;
        }

        if (mode.getValue().getName().equalsIgnoreCase("bed")) {
            final net.minecraft.util.Vec3 bedPos = PlayerUtil.getNearlyBlock(range.getValue().floatValue(), Blocks.bed);;
            if (bedPos == null) {
                renderBlock = null;
                return;
            }
            final BlockPos blockPos = new BlockPos(bedPos);
            renderBlock = blockPos;

            final Vector2f rotation = RotationUtil.calculate(bedPos);

            if (rotations.getValue()) RotationComponent.setRotations(rotation, 10, MovementFix.NORMAL);

            if (RayCastUtil.overBlock(EnumFacing.UP, blockPos, false)) {
                if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    mc.gameSettings.keyBindAttack.setPressed(true);
                    needUpdate = true;
                }
            } else {
                if (scatter.getValue()) {
                    PacketUtil.send(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.UP));
                    PacketUtil.send(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.UP));
                    needUpdate = true;
                } else {
                   if (mc.objectMouseOver != null) {
                       mc.gameSettings.keyBindAttack.setPressed(true);
                       needUpdate = true;
                   }
                }
            }
        }

        if (mode.getValue().getName().equalsIgnoreCase("watchdog")) {
            final net.minecraft.util.Vec3 bedPos = PlayerUtil.getNearlyBlock(range.getValue().floatValue(), Blocks.bed);;
            if (bedPos == null) {
                renderBlock = null;
                return;
            }

            boolean ignored = false;
            renderBlock = new BlockPos(bedPos);
            List<BlockPos> tempBlocks = new ArrayList<>();
            
            for (Vec3i offset : vec3List) {
                final BlockPos targetBlock = renderBlock.add(offset);

                final Block block = PlayerUtil.block(targetBlock);

                if (block == null || block instanceof BlockBed || mc.thePlayer.getDistanceSq(targetBlock) > mc.playerController.getBlockReachDistance()) continue;

                if (block instanceof BlockAir) {
                    ignored = true;
                    break;
                }

                tempBlocks.add(targetBlock);
            }

            tempBlocks.sort(Comparator.comparingDouble(vec3 -> {

                final double d0 = (mc.thePlayer.posX) - vec3.getX();
                final double d1 = (mc.thePlayer.posY - 1) - vec3.getY();
                final double d2 = (mc.thePlayer.posZ) - vec3.getZ();
                return MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);

            }));

            if (!ignored && !tempBlocks.isEmpty()) {
                renderBlock = tempBlocks.get(0);
            } else if (!ignored) {
                renderBlock = null;
                return;
            }
            

            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.getBlockPos() != null && mc.objectMouseOver.getBlockPos().equals(renderBlock)) {
                mc.gameSettings.keyBindAttack.setPressed(true);
                needUpdate = true;
                if (mc.playerController.curBlockDamageMP >= 1.0f) {
                    RotationComponent.setRotations(RotationUtil.calculate(bedPos), 10, MovementFix.OFF);
                }
            } else {
                RotationComponent.setRotations(RotationUtil.calculate(bedPos), 10, MovementFix.OFF);
            }
        }
    };

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (GUIDetectionComponent.inGUI()) return;

        final double range = this.range.getValue().doubleValue();

        switch (mode.getValue().getName()) {
            case "Nearby":
                if (stopWatch.finished(delay.getValue().longValue())) {
                    new Thread(() -> nuke(range, event.getPosX(), event.getPosY(), event.getPosZ())).start();
                    stopWatch.reset();
                }
                break;

            case "Teleport":
                if (mc.gameSettings.keyBindUseItem.isKeyDown() && stopWatch.finished(delay.getValue().longValue())) {
                    final BlockPos target = mc.thePlayer.rayTrace(999, 1).getBlockPos();

                    if (mc.theWorld.getBlockState(target).getBlock() instanceof BlockAir) {
                        return;
                    }

                    if (mc.thePlayer.capabilities.allowFlying && !mc.thePlayer.capabilities.isFlying) {
                        final PlayerCapabilities capabilities = mc.thePlayer.capabilities;
                        capabilities.isFlying = true;
                        PacketUtil.sendNoEvent(new C13PacketPlayerAbilities(capabilities));
                    }

                    new Thread(() -> {
                        final List<Vec3> path = MainPathFinder.computePath(new Vec3(event.getPosX(), event.getPosY(), event.getPosZ()), new Vec3(target.getX(), target.getY(), target.getZ()), false);

                        if (path == null) {
                            return;
                        }

                        for (final Vec3 vec : path) {
                            PacketUtil.sendNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(vec.getX(), vec.getY(), vec.getZ(), false));
                        }

                        nuke(range, target.getX(), target.getY(), target.getZ());
                        Collections.reverse(path);

                        for (final Vec3 vec : path) {
                            PacketUtil.sendNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(vec.getX(), vec.getY(), vec.getZ(), false));
                        }
                    }).start();

                    stopWatch.reset();
                }
                break;
        }


    };


    @EventLink()
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {

        final Packet<?> p = event.getPacket();

        if (p instanceof S02PacketChat) {
            final S02PacketChat wrapper = (S02PacketChat) p;

            if (!wrapper.isChat() && wrapper.getChatComponent().getUnformattedText().equals("You can't build outside the plot!")) {
                event.setCancelled(true);
            }
        }
    };

    @EventLink()
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        this.toggle();
    };

    private void nuke(final double range, final double x, final double y, final double z) {
        for (double posX = -range; posX < range; posX++) {
            for (double posY = range; posY > -range; posY--) {
                for (double posZ = -range; posZ < range; posZ++) {
                    if (scatter.getValue() && !((mc.thePlayer.ticksExisted % 2 == 0 ? posX : posZ) % 2 == 0)) {
                        continue;
                    }

                    final BlockPos blockPos = new BlockPos(x + posX, y + posY, z + posZ);
                    final Block block = mc.theWorld.getBlockState(blockPos).getBlock();

                    if (block instanceof BlockAir) {
                        continue;
                    }

                    if (rotations.getValue()) {
                        final Vector2f rotations = RotationUtil.calculate(new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                        PacketUtil.sendNoEvent(new C03PacketPlayer.C05PacketPlayerLook(rotations.x, rotations.y, false));
                    }
                    PacketUtil.sendNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.UP));

                    if (swing.getValue()) {
                        mc.thePlayer.swingItem();
                    }
                }
            }
        }

//        final BlockPos blockPos = new BlockPos(x, y, z);
//        final Block block = mc.theWorld.getBlockState(blockPos).getBlock();
//
//        if (block instanceof BlockAir) {
//            return;
//        }
//
//        if (rotations.getValue()) {
//            final Vector2f rotations = RotationUtil.calculate(new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
//            PacketUtil.sendNoEvent(new C03PacketPlayer.C05PacketPlayerLook(rotations.x, rotations.y, false));
//        }
//        PacketUtil.sendNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
//        PacketUtil.sendNoEvent(new C08PacketPlayerBlockPlacement(blockPos, EnumFacing.UP.getIndex(), mc.thePlayer.getHeldItem(), 255, 255, 255));
//        PacketUtil.sendNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
//
//        if (swing.getValue()) {
//            mc.thePlayer.swingItem();
//        }
    }
}