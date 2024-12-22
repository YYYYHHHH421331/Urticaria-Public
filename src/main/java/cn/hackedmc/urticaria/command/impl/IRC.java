package cn.hackedmc.urticaria.command.impl;

import cn.hackedmc.urticaria.command.Command;

public final class IRC extends Command {

    public IRC() {
        super("command.irc.description", "irc");
    }

    @Override
    public void execute(final String[] args) {
        if (args.length <= 1) {
            error(String.format(".%s <message>", args[0]));
        } else {
        }
    }
}