package cn.hackedmc.urticaria.newevent.impl.motion;

import cn.hackedmc.urticaria.newevent.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LavaEvent implements Event {
    private boolean lava;
}
