package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.impl.player.ChestAura;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.render.Render3DEvent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.util.render.ColorUtil;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.BlockPos;

import java.util.ConcurrentModificationException;

@Rise
@ModuleInfo(name = "module.render.chestesp.name", description = "module.render.chestesp.description", category = Category.RENDER)
public final class ChestESP extends Module {
    private final BooleanValue chestAuraCheck = new BooleanValue("Opened Chest Render", this, true);

    @EventLink()
    public final Listener<Render3DEvent> onRender3D = event -> {

        final Runnable runnable = () -> {
            try {
                mc.theWorld.loadedTileEntityList.forEach(entity -> {
                    if (entity instanceof TileEntityChest || entity instanceof TileEntityEnderChest || entity instanceof TileEntityFurnace || entity instanceof TileEntityBrewingStand) {
                        final BlockPos bp = entity.getPos();
                        RendererLivingEntity.setShaderBrightnessWithAlpha(ColorUtil.withAlpha(chestAuraCheck.getValue() && ChestAura.INSTANCE.isEnabled() ? (bp != null && ChestAura.INSTANCE.found.contains(bp) ? getTheme().getSecondColor() : getTheme().getFirstColor()) : getTheme().getFirstColor(), 100));
                        TileEntityRendererDispatcher.instance.renderBasicTileEntity(entity, event.getPartialTicks());
                        RendererLivingEntity.unsetShaderBrightness();
                    }
                });
            } catch (final ConcurrentModificationException ignored) {
            }
        };

        runnable.run();
        NORMAL_POST_BLOOM_RUNNABLES.add(runnable);
    };
}