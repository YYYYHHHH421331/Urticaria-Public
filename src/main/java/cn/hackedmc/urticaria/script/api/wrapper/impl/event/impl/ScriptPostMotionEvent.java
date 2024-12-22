package cn.hackedmc.urticaria.script.api.wrapper.impl.event.impl;


import cn.hackedmc.urticaria.newevent.impl.motion.PostMotionEvent;
import cn.hackedmc.urticaria.script.api.wrapper.impl.event.ScriptEvent;

/**
 * @author Auth
 * @since 9/07/2022
 */
public class ScriptPostMotionEvent extends ScriptEvent<PostMotionEvent> {

    public ScriptPostMotionEvent(final PostMotionEvent wrappedEvent) {
        super(wrappedEvent);
    }

    @Override
    public String getHandlerName() {
        return "onPostMotion";
    }
}
