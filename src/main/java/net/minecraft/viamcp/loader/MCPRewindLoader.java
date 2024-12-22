package net.minecraft.viamcp.loader;

import com.viaversion.viarewind.api.ViaRewindPlatform;
import com.viaversion.viaversion.api.Via;

import java.io.File;
import java.util.logging.Logger;

public class MCPRewindLoader implements ViaRewindPlatform {
    private final File file;

    public MCPRewindLoader(final File file) {
        this.init(this.file = file.toPath().resolve("ViaRewind").resolve("config.yml").toFile());
    }

    @Override
    public void init(File configFile) {
        ViaRewindPlatform.super.init(configFile);
    }

    @Override
    public Logger getLogger() {
        return Via.getPlatform().getLogger();
    }

    @Override
    public File getDataFolder() {
        return file;
    }
}
