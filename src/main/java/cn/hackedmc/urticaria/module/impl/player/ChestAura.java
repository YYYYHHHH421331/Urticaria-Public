package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.component.impl.player.BlinkComponent;
import cn.hackedmc.urticaria.component.impl.player.RotationComponent;
import cn.hackedmc.urticaria.component.impl.player.SlotComponent;
import cn.hackedmc.urticaria.component.impl.player.rotationcomponent.MovementFix;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.combat.KillAura;
import cn.hackedmc.urticaria.module.impl.movement.Phase;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.other.PossibleClickEvent;
import cn.hackedmc.urticaria.newevent.impl.other.WorldChangeEvent;
import cn.hackedmc.urticaria.util.RandomUtil;
import cn.hackedmc.urticaria.util.RayCastUtil;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.util.rotation.RotationUtil;
import cn.hackedmc.urticaria.util.vector.Vector2f;
import cn.hackedmc.urticaria.util.vector.Vector3d;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.BoundsNumberValue;
import cn.hackedmc.urticaria.value.impl.ListValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockFurnace;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Rotations;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import util.time.StopWatch;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "module.player.chestaura.name", description = "module.player.chestaura.description", category = Category.PLAYER)
public class ChestAura extends Module {
    public static ChestAura INSTANCE;
    private final NumberValue range = new NumberValue("Range", this, 3, 1, 6, 1);
    private final BooleanValue rotation = new BooleanValue("Rotation", this, false);
    private final BoundsNumberValue rotationSpeed = new BoundsNumberValue("Rotation Speed", this, 5, 10, 1, 10, 1, () -> !rotation.getValue());
    private final BooleanValue movementCorrection = new BooleanValue("Movement Correction", this, true, () -> !rotation.getValue());
    private final BoundsNumberValue delay = new BoundsNumberValue("Delay", this, 50, 100, 0, 5000, 50);
    private final BooleanValue noSwing = new BooleanValue("No Swing", this, false);
    private final StopWatch stopWatch = new StopWatch();
    private final StopWatch noUse = new StopWatch();
    private long nextWait = 0;
    private BlockPos blockPos;
    public final List<BlockPos> found = new ArrayList<>();

    public ChestAura() {
        this.setKeyCode(Keyboard.KEY_X);

        INSTANCE = this;
    }

    @Override
    protected void onEnable() {
        stopWatch.reset();
        found.clear();
    }

    @EventLink
    private final Listener<WorldChangeEvent> onWorld = event -> {
        found.clear();
        stopWatch.reset();
    };

    @EventLink
    private final Listener<PossibleClickEvent> onPossibleClick = event -> {
        if (mc.thePlayer == null || mc.theWorld == null || BlinkComponent.blinking || Phase.INSTANCE.isEnabled() || !mc.playerController.gameIsSurvivalOrAdventure() || !noUse.finished(1000L) || !stopWatch.finished(nextWait) || mc.currentScreen != null || KillAura.INSTANCE.target != null || Scaffold.INSTANCE.isEnabled() || mc.thePlayer.isUsingItem() || mc.currentScreen != null) {
            return;
        }

        if (!found.contains(mc.objectMouseOver.getBlockPos()) && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && isContainer(mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock())) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, SlotComponent.getItemStack(), mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec)) {
                if (noSwing.getValue()) PacketUtil.send(new C0APacketAnimation());
                else InstanceAccess.mc.thePlayer.swingItem();
            }
        }
    };

    @EventLink
    private final Listener<PreUpdateEvent> onPreMotion = event -> {
        if (mc.currentScreen != null && mc.thePlayer.inventoryContainer != null) {
            if (blockPos != null) found.add(blockPos);
            else if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && isContainer(PlayerUtil.block(mc.objectMouseOver.getBlockPos()))) found.add(mc.objectMouseOver.getBlockPos());
            nextWait = RandomUtil.nextInt(delay.getValue().intValue(), delay.getSecondValue().intValue());
            stopWatch.reset();
            blockPos = null;
        }

        if (mc.thePlayer == null || mc.theWorld == null || BlinkComponent.blinking || Phase.INSTANCE.isEnabled() || !mc.playerController.gameIsSurvivalOrAdventure() || mc.currentScreen != null) {
            noUse.reset();
            blockPos = null;
            return;
        }

        if (!noUse.finished(100L) || !stopWatch.finished(nextWait) || mc.currentScreen != null || KillAura.INSTANCE.target != null || Scaffold.INSTANCE.isEnabled() || AutoGApple.eating || mc.thePlayer.isUsingItem()) return;

        int reach = range.getValue().intValue();
        /*
        if (KillAura.INSTANCE.range.getValue().floatValue() > 3){
            Client.INSTANCE.getModuleManager().get(ChestAura.class).onDisable();
        }

         */

        boolean foundOne = blockPos != null;
        for (float y = reach; y >= -reach && !foundOne; y -= 1.0f) {
            for (float x = -reach; x <= reach && !foundOne; x += 1.0f) {
                for (float z = -reach; z <= reach; z += 1.0f) {
                    BlockPos pos = new BlockPos(mc.thePlayer).add(x, y, z);
                    Block block = PlayerUtil.block(pos);
                    if (mc.thePlayer.getDistance(pos.getX(), pos.getY(), pos.getZ()) > (double)mc.playerController.getBlockReachDistance() || !isContainer(block) || found.contains(pos)) continue;
                    Vector2f rotations = RotationUtil.getRotationBlock(pos);
                    final MovingObjectPosition cast = RayCastUtil.rayCast(rotations, reach);
                    if (cast != null && RayCastUtil.overBlock(rotations, pos)) {
                        this.blockPos = pos;
                        foundOne = true;
                        break;
                    }
                }
            }
        }

        if (foundOne) {
            final Vector2f rotations = RotationUtil.calculate(new Vec3(blockPos));
            final MovingObjectPosition cast = RayCastUtil.rayCast(rotations, reach);

            if (cast == null || !RayCastUtil.overBlock(rotations, blockPos)) {
                blockPos = null;
            } else {
                if (rotation.getValue()) {
                    RotationComponent.setRotations(rotations, RandomUtil.nextInt(rotationSpeed.getValue().intValue(), rotationSpeed.getSecondValue().intValue()), movementCorrection.getValue() ? MovementFix.NORMAL : MovementFix.OFF);
                } else {
                    if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, SlotComponent.getItemStack(), cast.getBlockPos(), cast.sideHit, cast.hitVec)) {
                        if (noSwing.getValue()) PacketUtil.send(new C0APacketAnimation());
                        else InstanceAccess.mc.thePlayer.swingItem();
                    }
                    found.add(blockPos);
                    nextWait = RandomUtil.nextInt(delay.getValue().intValue(), delay.getSecondValue().intValue());
                    stopWatch.reset();
                }
            }
        } else {
            blockPos = null;
        }
    };

    private boolean isContainer(Block block) {
        return block instanceof BlockChest || block instanceof BlockFurnace || block instanceof BlockBrewingStand;
    }
}
