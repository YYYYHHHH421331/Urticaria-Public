package cn.hackedmc.urticaria.script.api.wrapper.impl.event;

import cn.hackedmc.urticaria.newevent.Event;
import cn.hackedmc.urticaria.script.api.wrapper.ScriptWrapper;

/**
 * @author Strikeless
 * @since 23.06.2022
 */
public abstract class ScriptEvent<T extends Event> extends ScriptWrapper<T> {

    public ScriptEvent(final T wrappedEvent) {
        super(wrappedEvent);
    }

    public abstract String getHandlerName();
}
