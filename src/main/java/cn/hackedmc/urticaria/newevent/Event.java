package cn.hackedmc.urticaria.newevent;

import cn.hackedmc.urticaria.script.api.wrapper.impl.event.ScriptEvent;

public interface Event {
    public default ScriptEvent<? extends Event> getScriptEvent() {
        return null;
    }
}
