package cn.hackedmc.urticaria.component.impl.render.notificationcomponent;

import cn.hackedmc.urticaria.module.impl.render.Interface;
import cn.hackedmc.urticaria.util.animation.Animation;
import cn.hackedmc.urticaria.util.animation.Easing;
import lombok.Getter;
import lombok.Setter;
import util.time.StopWatch;

@Getter
public class Notification {
    private final String title;
    private final String describe;
    private final Type type;
    private final long time;
    public final StopWatch stopWatch;
    @Setter
    private Animation notificationY;
    private final Animation notificationOut;
    private final Animation notificationIn;
    @Setter
    private int state;

    public Notification(String title, String describe, Type type, long time) {
        this.title = title;
        this.describe = describe;
        this.type = type;
        this.time = time;
        this.state = 0;
        this.stopWatch = new StopWatch();

        switch (Interface.INSTANCE.notifyMode.getValue().getName().toLowerCase()) {
            default:
            case "basic": {
                notificationOut = new Animation(Easing.EASE_IN_QUINT, 500);
                notificationIn = new Animation(Easing.EASE_OUT_QUINT, 500);

                break;
            }

            case "central": {
                notificationOut = new Animation(Easing.EASE_IN_BACK, 500);
                notificationIn = new Animation(Easing.EASE_BOUNCE, 500);

                break;
            }
        }
    }

    public enum Type {
        INFO,
        WARNING,
        SUCCESS,
        ERROR
    }
}
