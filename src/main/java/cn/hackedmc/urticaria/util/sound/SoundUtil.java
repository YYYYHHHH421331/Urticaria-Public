package cn.hackedmc.urticaria.util.sound;

import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SoundUtil implements InstanceAccess {

    private int ticksExisted;

    public void toggleSound(final boolean enable) {
        if (mc.thePlayer != null && mc.thePlayer.ticksExisted != ticksExisted) {
            if (enable) {
                playSound("urticaria.toggle.enable");
            } else {
                playSound("urticaria.toggle.disable");
            }
            ticksExisted = mc.thePlayer.ticksExisted;
        }
    }

    public void playSound(final String sound) {
        playSound(sound, 1, 1);
    }

    public void playSound(final String sound, final float volume, final float pitch) {
        mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, sound, volume, pitch, false);
    }
}