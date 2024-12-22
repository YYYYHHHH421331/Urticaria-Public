package cn.hackedmc.urticaria.newevent.impl.render;


import cn.hackedmc.urticaria.newevent.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.ScaledResolution;

@Getter
@AllArgsConstructor
public final class RenderHungerEvent implements Event {

    private final ScaledResolution scaledResolution;

}
