package cn.hackedmc.urticaria.component.impl.player;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.Component;

@Rise
public final class LastConnectionComponent extends Component {
    public static String ip;
    public static int port;

    public static String getFullIp() {
        return ip + ":" + port;
    }
}