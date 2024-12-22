package cn.hackedmc.urticaria.module.impl.player.scaffold.sprint;

import cn.hackedmc.urticaria.module.impl.player.Scaffold;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.util.rotation.RotationUtil;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import util.time.StopWatch;

public class HuaYuTingSprint extends Mode<Scaffold> {
    private final BooleanValue slow = new BooleanValue("Slow", this, false);
    private final NumberValue slowTick = new NumberValue("Slow Tick", this, 3, 1, 5, 1);

    private final StopWatch lagTime = new StopWatch();

    public HuaYuTingSprint(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink
    private final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof S08PacketPlayerPosLook) {
            lagTime.reset();
        }
    };

    @Override
    public void onDisable() {
        mc.gameSettings.keyBindSprint.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindSprint));
        mc.gameSettings.keyBindForward.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindForward));
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mc.thePlayer == null || mc.gameSettings == null) return;

        if (lagTime.finished(3000L)) {
            mc.gameSettings.keyBindSprint.setPressed(mc.thePlayer.onGround);

            mc.gameSettings.keyBindForward.setPressed((!slow.getValue() || mc.thePlayer.offGroundTicks < this.getParent().getTellyTick() + slowTick.getValue().intValue() - (GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && !RotationUtil.isLineRotation() ? 1 : 0) || (!GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && RotationUtil.isLineRotation())) && GameSettings.isKeyDown(mc.gameSettings.keyBindForward));
        } else {
            mc.gameSettings.keyBindSprint.setPressed(false);
        }
    };
}
