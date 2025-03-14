package net.minecraft.client.network;

import cn.hackedmc.fucker.Fucker;
import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.module.impl.other.ServerProtocol;
import cn.hackedmc.urticaria.module.impl.other.protocols.hyt.forge.FMLHandshakeClientState;
import cn.hackedmc.urticaria.util.CryptUtil;
import cn.hackedmc.urticaria.util.vantage.HWIDUtil;
import cn.hackedmc.urticaria.util.web.Browser;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.network.login.server.S01PacketEncryptionRequest;
import net.minecraft.network.login.server.S02PacketLoginSuccess;
import net.minecraft.network.login.server.S03PacketEnableCompression;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.CryptManager;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;

public class NetHandlerLoginClient implements INetHandlerLoginClient {
    private static final Logger logger = LogManager.getLogger();
    private final Minecraft mc;
    private final GuiScreen previousGuiScreen;
    private final NetworkManager networkManager;
    private GameProfile gameProfile;

    public NetHandlerLoginClient(final NetworkManager p_i45059_1_, final Minecraft mcIn, final GuiScreen p_i45059_3_) {
        this.networkManager = p_i45059_1_;
        this.mc = mcIn;
        this.previousGuiScreen = p_i45059_3_;
    }

    public void handleEncryptionRequest(final S01PacketEncryptionRequest packetIn) {
        final SecretKey secretkey = CryptManager.createNewSharedKey();
        final String s = packetIn.getServerId();
        final PublicKey publickey = packetIn.getPublicKey();
        final String s1 = (new BigInteger(CryptManager.getServerIdHash(s, publickey, secretkey))).toString(16);

        if (this.mc.getCurrentServerData() != null && this.mc.getCurrentServerData().func_181041_d()) {
            try {
                this.getSessionService().joinServer(this.mc.getSession().getProfile(), this.mc.getSession().getToken(), s1);
            } catch (final AuthenticationException var10) {
                logger.warn("Couldn't connect to auth servers but will continue to join LAN");
            }
        } else {
            try {
                if (/*Fucker.login && !Fucker.free && */Client.INSTANCE.getAltManager() != null && Client.INSTANCE.getAltManager().isNetease) {
                    final JsonObject data = new JsonObject();
                    data.addProperty("userId", mc.session.getPlayerID());
                    data.addProperty("userToken", mc.session.getToken());
                    data.addProperty("serverId", s1);
                    data.addProperty("roleName", mc.session.getUsername());
//                    new AuthenticationCpp().Authentication(25565, s1);
                } else {
                    this.getSessionService().joinServer(this.mc.getSession().getProfile(), this.mc.getSession().getToken(), s1);
                }
            } catch (final AuthenticationUnavailableException var7) {
                this.networkManager.closeChannel(new ChatComponentTranslation("disconnect.loginFailedInfo", new ChatComponentTranslation("disconnect.loginFailedInfo.serversUnavailable")));
                return;
            } catch (final InvalidCredentialsException var8) {
                this.networkManager.closeChannel(new ChatComponentTranslation("disconnect.loginFailedInfo", new ChatComponentTranslation("disconnect.loginFailedInfo.invalidSession")));
                return;
            } catch (final AuthenticationException authenticationexception) {
                this.networkManager.closeChannel(new ChatComponentTranslation("disconnect.loginFailedInfo", authenticationexception.getMessage()));
                return;
            }
        }

        this.networkManager.sendPacket(new C01PacketEncryptionResponse(secretkey, publickey, packetIn.getVerifyToken()), new GenericFutureListener<Future<? super Void>>() {
            public void operationComplete(final Future<? super Void> p_operationComplete_1_) throws Exception {
                NetHandlerLoginClient.this.networkManager.enableEncryption(secretkey);
            }
        });
    }

    private MinecraftSessionService getSessionService() {
        return this.mc.getSessionService();
    }

    public void handleLoginSuccess(final S02PacketLoginSuccess packetIn) {
        this.gameProfile = packetIn.getProfile();
        this.networkManager.setConnectionState(EnumConnectionState.PLAY);
        this.networkManager.setNetHandler(new NetHandlerPlayClient(this.mc, this.previousGuiScreen, this.networkManager, this.gameProfile));
        final ServerProtocol serverProtocol = ServerProtocol.INSTANCE;
        if (serverProtocol.isEnabled() && serverProtocol.mode.getValue().getName().equalsIgnoreCase("HuaYuTing"))
            ServerProtocol.INSTANCE.huaYuTingProtocol.forgeChannel.currentState = FMLHandshakeClientState.START;
    }

    /**
     * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
     */
    public void onDisconnect(final IChatComponent reason) {
        this.mc.displayGuiScreen(new GuiDisconnected(this.previousGuiScreen, "connect.failed", reason));
    }

    public void handleDisconnect(final S00PacketDisconnect packetIn) {
        this.networkManager.closeChannel(packetIn.func_149603_c());
    }

    public void handleEnableCompression(final S03PacketEnableCompression packetIn) {
        if (!this.networkManager.isLocalChannel()) {
            this.networkManager.setCompressionTreshold(packetIn.getCompressionTreshold());
        }
    }
}
