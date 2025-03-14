package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.component.impl.player.RotationComponent;
import cn.hackedmc.urticaria.component.impl.player.rotationcomponent.MovementFix;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.RayCastUtil;
import cn.hackedmc.urticaria.util.math.MathUtil;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.rotation.RotationUtil;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.BoundsNumberValue;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.viamcp.ViaMCP;
import util.time.StopWatch;

@ModuleInfo(name = "module.player.antifireball.name", description = "module.player.antifireball.description", category = Category.PLAYER)
public class AntiFireBall extends Module {
    private final BooleanValue rotation = new BooleanValue("Rotation",this,  true);
    private final BoundsNumberValue rotationSpeed = new BoundsNumberValue("Rotation Speed", this, 10, 10, 0, 10, 0.1, () -> !rotation.getValue());

    private final BooleanValue moveFix = new BooleanValue("Movement Correction",this,  false, () -> !rotation.getValue());
    private final BooleanValue rayCast = new BooleanValue("Ray Cast", this, false, () -> !rotation.getValue());
    private final BooleanValue noSwing = new BooleanValue("No Swing", this, false);
    private final StopWatch timerUtil = new StopWatch();

    @Override
    protected void onEnable() {
        timerUtil.reset();
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        for(Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityFireball && mc.thePlayer.getDistanceToEntity(entity) < 5.5F && this.timerUtil.finished(300L)) {
                this.timerUtil.reset();
                if (this.rotation.getValue()) {
                    RotationComponent.setRotations(RotationUtil.getRotationsNonLivingEntity(entity), (double) MathUtil.getRandom2(this.rotationSpeed.getValue().intValue(), this.rotationSpeed.getSecondValue().intValue()), this.moveFix.getValue() ? MovementFix.NORMAL : MovementFix.OFF);
                }
                if (!rayCast.getValue() || RayCastUtil.isEntity(entity, RayCastUtil.rayCast(RotationComponent.rotations, 5.5F))) {
                    if (ViaMCP.getInstance().getVersion() <= ProtocolVersion.v1_8.getVersion()) {
                        if (this.noSwing.getValue()) {
                            PacketUtil.send(new C0APacketAnimation());
                        } else {
                            mc.thePlayer.swingItem();
                        }
                    }
                    PacketUtil.send(new C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK));
                    if (ViaMCP.getInstance().getVersion() > ProtocolVersion.v1_8.getVersion()) {
                        if (this.noSwing.getValue()) {
                            PacketUtil.send(new C0APacketAnimation());
                        } else {
                            mc.thePlayer.swingItem();
                        }
                    }
                }
            }
        }
    };
}
