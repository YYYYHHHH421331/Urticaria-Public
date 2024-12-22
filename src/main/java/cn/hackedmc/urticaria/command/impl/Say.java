package cn.hackedmc.urticaria.command.impl;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.command.Command;
import cn.hackedmc.urticaria.util.chat.ChatUtil;

/**
 * @author Auth
 * @since 3/02/2022
 */
@Rise
public final class Say extends Command {

    public Say() {
        super("command.say.description", "say", "chat");
    }

    @Override
    public void execute(final String[] args) {
        if (args.length <= 1) {
            error(String.format(".%s <message>", args[0]));
        } else {
            ChatUtil.send(String.join(" ", args).substring(3).trim());
            ChatUtil.display("command.say.sent");
        }
    }
}
