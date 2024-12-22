package cn.hackedmc.urticaria.script.api.wrapper.impl.event.impl;


import cn.hackedmc.urticaria.newevent.impl.input.ClickEvent;
import cn.hackedmc.urticaria.script.api.wrapper.impl.event.ScriptEvent;

/**
 * @author Auth
 * @since 9/07/2022
 */
public class ScriptClickEvent extends ScriptEvent<ClickEvent> {

    public ScriptClickEvent(final ClickEvent wrappedEvent) {
        super(wrappedEvent);
    }

    @Override
    public String getHandlerName() {
        return "onClick";
    }
}
