package cn.hackedmc.urticaria.script.api;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.other.TickEvent;
import cn.hackedmc.urticaria.script.api.wrapper.impl.ScriptWorld;

/**
 * @author Strikeless
 * @since 20.06.2022
 */
public class WorldAPI extends ScriptWorld {

    public WorldAPI() {
        super(MC.theWorld);

        Client.INSTANCE.getEventBus().register(this);
    }

    @EventLink()
    public final Listener<TickEvent> onTick = event -> {
        if (this.wrapped == null) {
            this.wrapped = MC.theWorld;
        }
    };
}
