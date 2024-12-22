package net.minecraft.viamcp.platform;

import com.viaversion.viaversion.ViaAPIBase;
import com.viaversion.viaversion.api.Via;

import java.util.SortedSet;
import java.util.UUID;

public class MCPViaAPI extends ViaAPIBase<UUID> {
    @Override
    public int majorVersion() {
        return super.majorVersion();
    }

    @Override
    public int apiVersion() {
        return super.apiVersion();
    }

    @Override
    public int getPlayerVersion(UUID uuid) {
        return Via.getAPI().getPlayerVersion(uuid);
    }

    @Override
    public SortedSet<Integer> getSupportedVersions() {
        return Via.getAPI().getSupportedVersions();
    }

    @Override
    public SortedSet<Integer> getFullSupportedVersions() {
        return Via.getAPI().getFullSupportedVersions();
    }
}
