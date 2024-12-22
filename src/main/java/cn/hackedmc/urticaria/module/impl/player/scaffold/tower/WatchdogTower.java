package cn.hackedmc.urticaria.module.impl.player.scaffold.tower;

import cn.hackedmc.urticaria.module.impl.player.Scaffold;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.MoveEvent;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.block.BlockAir;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;

public class WatchdogTower extends Mode<Scaffold> {
    public WatchdogTower(String name, Scaffold parent) {
        super(name, parent);
    }

    private boolean towering;
    private int towerTicks;

    @Override
    public void onEnable() {
        towering = false;
        towerTicks = 0;
    }

    @EventLink
    private final Listener<MoveEvent> onMove = event -> {
        boolean airUnder = negativeExpand(0.239);
        if (MoveUtil.isMoving() && MoveUtil.speed() > 0.1 && !mc.thePlayer.isPotionActive(Potion.jump)) {
            double towerSpeed = isGoingDiagonally(0.1) ? 0.22 : 0.29888888;
            if (!mc.thePlayer.onGround) {
                if (this.towering) {
                    if (this.towerTicks == 2) {
                        event.setPosY(Math.floor(mc.thePlayer.posY + 1.0) - mc.thePlayer.posY);
                    } else if (this.towerTicks == 3) {
                        if (canTower() && !airUnder) {
                            event.setPosY(mc.thePlayer.motionY = 0.4198499917984009);
                            MoveUtil.strafe((float) towerSpeed - this.randomAmount());
                            this.towerTicks = 0;
                        } else {
                            this.towering = false;
                        }
                    }
                }
            } else {
                this.towering = canTower() && !airUnder;
                if (this.towering) {
                    this.towerTicks = 0;
                    mc.thePlayer.jumpTicks = 0;
                    if (event.getPosY() > 0.0) {
                        event.setPosY(mc.thePlayer.motionY = 0.4198479950428009);
                        MoveUtil.strafe((float) towerSpeed - this.randomAmount());
                    }
                }
            }

            ++this.towerTicks;
        }
    };

    public boolean canTower() {
//        if (Scaffold.instance.totalBlocks() == 0) return false;
        if (mc.currentScreen != null) return false;
        if (!mc.gameSettings.keyBindJump.isKeyDown()) {
            return false;
        } else return mc.thePlayer.hurtTime < 9;
    }

    public static boolean negativeExpand(double negativeExpandValue) {
        return mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + negativeExpandValue, mc.thePlayer.posY - 1, mc.thePlayer.posZ + negativeExpandValue)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX - negativeExpandValue, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ - negativeExpandValue)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX - negativeExpandValue, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + negativeExpandValue, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ + negativeExpandValue)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ - negativeExpandValue)).getBlock() instanceof BlockAir;
    }

    public static boolean isGoingDiagonally(double amount) {
        return Math.abs(mc.thePlayer.motionX) > amount && Math.abs(mc.thePlayer.motionZ) > amount;
    }

    private double randomAmount() {
        return 8.0E-4 + Math.random() * 0.008;
    }
}
