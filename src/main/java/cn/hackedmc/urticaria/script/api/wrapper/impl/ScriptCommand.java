package cn.hackedmc.urticaria.script.api.wrapper.impl;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.command.Command;
import cn.hackedmc.urticaria.script.api.wrapper.ScriptHandlerWrapper;

/**
 * @author Strikeless
 * @since 15.05.2022
 */
public final class ScriptCommand extends ScriptHandlerWrapper<Command> {

    public ScriptCommand(final Command wrapped) {
        super(wrapped);
    }

    public void unregister() {
        Client.INSTANCE.getCommandManager().remove(this.wrapped);
    }

    // TODO: Make command execution again

    public String getName() {
        return this.wrapped.getExpressions()[0];
    }

    public String getDescription() {
        return this.wrapped.getDescription();
    }
}
