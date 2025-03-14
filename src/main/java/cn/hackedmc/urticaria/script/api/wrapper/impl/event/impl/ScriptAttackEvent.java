package cn.hackedmc.urticaria.script.api.wrapper.impl.event.impl;

import cn.hackedmc.urticaria.newevent.impl.other.AttackEvent;
import cn.hackedmc.urticaria.script.api.wrapper.impl.ScriptEntity;
import cn.hackedmc.urticaria.script.api.wrapper.impl.event.CancellableScriptEvent;

/**
 * @author Auth
 * @since 10/07/2022
 */
public class ScriptAttackEvent extends CancellableScriptEvent<AttackEvent> {

    public ScriptAttackEvent(final AttackEvent wrappedEvent) {
        super(wrappedEvent);
    }

    public void setTarget(final ScriptEntity target) {
        this.wrapped.setTarget(MC.theWorld.getEntityByID(target.getEntityId()));
    }

    public ScriptEntity getTarget() {
        return new ScriptEntity(this.wrapped.getTarget());
    }

    @Override
    public String getHandlerName() {
        return "onAttack";
    }
}
