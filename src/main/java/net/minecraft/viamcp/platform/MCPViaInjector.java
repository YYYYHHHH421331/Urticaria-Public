package net.minecraft.viamcp.platform;

import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.minecraft.viamcp.ViaMCP;
import net.minecraft.viamcp.network.VLBPipeline;

public class MCPViaInjector implements ViaInjector {
    @Override
    public void inject() {
        // In a nutshell, this is not forge
    }

    @Override
    public void uninject() {
        // Update! Still not forge!
    }

    @Override
    public ProtocolVersion getServerProtocolVersion() {
        return ProtocolVersion.getProtocol(ViaMCP.PROTOCOL_VERSION);
    }

    @Override
    public String getEncoderName() {
        return VLBPipeline.VIA_ENCODER_HANDLER_NAME;
    }

    @Override
    public String getDecoderName() {
        return VLBPipeline.VIA_DECODER_HANDLER_NAME;
    }

    @Override
    public JsonObject getDump() {
        final JsonObject obj = new JsonObject();
        return obj;
    }
}
