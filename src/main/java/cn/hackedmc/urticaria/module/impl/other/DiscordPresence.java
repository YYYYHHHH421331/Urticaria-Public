package cn.hackedmc.urticaria.module.impl.other;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.other.TickEvent;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

@ModuleInfo(name = "module.other.discordrpc.name", description = "module.other.discordrpc.description", category = Category.OTHER, autoEnabled = true)
public class DiscordPresence extends Module {
    private boolean started;

    @EventLink
    private final Listener<TickEvent> onTick = event -> {
        if (!started) {
            final DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder("") {{
                setDetails("Urticaria " + Client.VERSION_FULL);
                setBigImage("urticaria", "");
                setStartTimestamps(System.currentTimeMillis());
            }};

            DiscordRPC.discordUpdatePresence(builder.build());

            final DiscordEventHandlers handlers = new DiscordEventHandlers();
            DiscordRPC.discordInitialize("1201253156550623302", handlers, true);

            new Thread(() -> {
                while (this.isEnabled()) {
                    DiscordRPC.discordRunCallbacks();
                }
            }, "Discord RPC Callback").start();

            started = true;
        }
    };

    @Override
    protected void onDisable() {
        DiscordRPC.discordShutdown();
        started = false;
    }
}
