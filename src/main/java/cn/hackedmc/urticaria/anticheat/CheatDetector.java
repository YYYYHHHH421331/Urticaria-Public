package cn.hackedmc.urticaria.anticheat;

import cn.hackedmc.urticaria.anticheat.alert.AlertManager;
import cn.hackedmc.urticaria.anticheat.check.manager.CheckManager;
import cn.hackedmc.urticaria.anticheat.data.PlayerData;
import cn.hackedmc.urticaria.anticheat.listener.RegistrationListener;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class CheatDetector {

    public final Map<UUID, PlayerData> playerMap = new ConcurrentHashMap<>();

    private final RegistrationListener registrationListener = new RegistrationListener();
    public final AlertManager alertManager = new AlertManager();

    public CheatDetector() {
        CheckManager.setup();
    }

    public void incrementTick() {
        for (PlayerData data : playerMap.values()) {
            if (Minecraft.getMinecraft().theWorld.playerEntities.contains(data.getPlayer())) {
                data.incrementTick();
            } else {
                registrationListener.handleDestroy(data.getPlayer().getUniqueID());
            }
        }
    }
}
