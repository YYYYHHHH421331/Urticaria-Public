package cn.hackedmc.urticaria.script.api.wrapper.impl.event.impl;

import cn.hackedmc.urticaria.newevent.impl.other.KillEvent;
import cn.hackedmc.urticaria.script.api.wrapper.impl.ScriptEntity;
import cn.hackedmc.urticaria.script.api.wrapper.impl.event.ScriptEvent;

public class ScriptKillEvent extends ScriptEvent<KillEvent>
{
	public ScriptEntity getEntity() {
		return new ScriptEntity(this.wrapped.getEntity());
	}
	public ScriptKillEvent(KillEvent wrappedEvent)
	{
		super(wrappedEvent);
	}

	@Override
	public String getHandlerName()
	{
		return "onKill";
	}
}
