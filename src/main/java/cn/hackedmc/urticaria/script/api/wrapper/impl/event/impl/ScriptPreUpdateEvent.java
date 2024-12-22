package cn.hackedmc.urticaria.script.api.wrapper.impl.event.impl;


import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.script.api.wrapper.impl.event.ScriptEvent;

/**
 * @author Auth
 * @since 9/07/2022
 */
public class ScriptPreUpdateEvent extends ScriptEvent<PreUpdateEvent> {

    public ScriptPreUpdateEvent(final PreUpdateEvent wrappedEvent) {
        super(wrappedEvent);
    }

    @Override
    public String getHandlerName() {
        return "onPreUpdate";
    }
}
