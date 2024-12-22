package cn.hackedmc.urticaria.newevent.impl.motion;


import cn.hackedmc.urticaria.newevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Alan
 * @since 13.03.2022
 */
@Getter
@Setter
@AllArgsConstructor
public class MinimumMotionEvent extends CancellableEvent {
    private double minimumMotion;
}
