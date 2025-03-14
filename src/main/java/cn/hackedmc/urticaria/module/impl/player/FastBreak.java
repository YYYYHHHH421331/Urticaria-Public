package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

@Rise
@ModuleInfo(name = "module.player.fastbreak.name", description = "module.player.fastbreak.description", category = Category.PLAYER)
public final class FastBreak extends Module {
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Percentage"))
            .add(new SubMode("Ticks"))
            .setDefault("Ticks");

    private final NumberValue speed = new NumberValue("Speed", this, 50, 0, 100, 1,  () -> mode.getValue().getName().equals("Ticks"));
    private final NumberValue ticks = new NumberValue("Ticks", this, 1, 1, 100, 1, () -> mode.getValue().getName().equals("Percentage"));

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {

        InstanceAccess.mc.playerController.blockHitDelay = 0;

        double percentageFaster = 0;

        switch (mode.getValue().getName()) {
            case "Percentage":
                percentageFaster = speed.getValue().doubleValue() / 100f;
                break;

            case "Ticks":
                if (InstanceAccess.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    BlockPos blockPos = InstanceAccess.mc.objectMouseOver.getBlockPos();
                    Block block = PlayerUtil.block(blockPos);

                    float blockHardness = block.getPlayerRelativeBlockHardness(InstanceAccess.mc.thePlayer, InstanceAccess.mc.theWorld, blockPos);
                    percentageFaster = blockHardness * ticks.getValue().intValue();
                }
                break;
        }

        if (InstanceAccess.mc.playerController.curBlockDamageMP > 1 - percentageFaster) {
            InstanceAccess.mc.playerController.curBlockDamageMP = 1;
        }
    };
}
