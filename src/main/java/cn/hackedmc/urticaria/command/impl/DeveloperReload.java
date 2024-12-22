package cn.hackedmc.urticaria.command.impl;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.command.Command;
import cn.hackedmc.urticaria.module.api.DevelopmentFeature;
import cn.hackedmc.urticaria.util.chat.ChatUtil;

/**
 * @author Alan
 * @since 10/19/2021
 */
@Rise
@DevelopmentFeature
public final class DeveloperReload extends Command {

    public DeveloperReload() {
        super("Reloads the client", "developerreload", "dr");
    }

    @Override
    public void execute(final String[] args) {
        Client.INSTANCE.terminate();
        Client.INSTANCE.initClient();
        ChatUtil.display("Reloaded Urticaria");
    }
}