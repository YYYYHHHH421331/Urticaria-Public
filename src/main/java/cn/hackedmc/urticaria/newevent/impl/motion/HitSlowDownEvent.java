package cn.hackedmc.urticaria.newevent.impl.motion;

import cn.hackedmc.urticaria.newevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class HitSlowDownEvent extends CancellableEvent {
    public double slowDown;
    public boolean sprint;
}
