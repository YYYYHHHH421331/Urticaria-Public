package cn.hackedmc.urticaria.util.player;

import cn.hackedmc.urticaria.component.impl.player.LastConnectionComponent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import lombok.experimental.UtilityClass;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.OldServerPinger;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Auth
 * @since 30/06/2022
 */

@UtilityClass
public class ServerUtil implements InstanceAccess {

    public final Map<String, Boolean> cachedServers = new HashMap<>();

    private final OldServerPinger pinger = new OldServerPinger();

    public static String getRemoteIp() {
        String serverIp = "Singleplayer";

        if (mc.theWorld.isRemote) {
            final ServerData serverData = mc.getCurrentServerData();
            if(serverData != null)
                serverIp = serverData.serverIP;
        }

        return serverIp;
    }

    public boolean isOnServer(final String server) {
        if (cachedServers.containsKey(server)) {
            return cachedServers.get(server);
        } else {
            final boolean isOnServer = !mc.isIntegratedServerRunning() && StringUtils.containsIgnoreCase(LastConnectionComponent.ip, server);
            cachedServers.put(server, isOnServer);
            return isOnServer;
        }
    }

    public ServerData isOnline(final String ip, final int port, final int timeout) {
        try {
            final String address = ip + ":" + port;
            final ServerData data = new ServerData(address, address, false);
            pinger.ping(data, timeout);

            return data;
        } catch (final Exception ignored) {
        }

        return null;
    }
}