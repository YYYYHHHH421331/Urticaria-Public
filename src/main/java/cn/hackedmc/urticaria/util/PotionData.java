package cn.hackedmc.urticaria.util;

import cn.hackedmc.urticaria.util.render.Animator;
import lombok.Getter;
import net.minecraft.potion.Potion;

public class PotionData {
    @Getter
    public final Potion potion;
    public int maxTimer = 0;
    public float animationX = 0;
    @Getter
    public final Animator translate;
    @Getter
    public final int level;
    public PotionData(Potion potion, Animator translate, int level) {
        this.potion = potion;
        this.translate = translate;
        this.level = level;
    }
}