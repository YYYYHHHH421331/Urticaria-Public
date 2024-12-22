package cn.hackedmc.urticaria.newevent.impl.render;


import cn.hackedmc.urticaria.newevent.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class Render3DEvent implements Event {

    private final float partialTicks;
}
