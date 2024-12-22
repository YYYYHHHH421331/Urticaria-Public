package cn.hackedmc.urticaria.newevent.impl.render;

import cn.hackedmc.urticaria.newevent.Event;
import cn.hackedmc.urticaria.util.vector.Vector2f;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class LookEvent implements Event {
    private Vector2f rotation;
}
