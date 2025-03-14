package cn.hackedmc.urticaria.util.account.microsoft;

import cn.hackedmc.urticaria.util.web.Browser;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MicrosoftLogin {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static class LoginData {
        public String mcToken;
        public String newRefreshToken;
        public String uuid, username;

        public LoginData() {
        }

        public LoginData(final String mcToken, final String newRefreshToken, final String uuid, final String username) {
            this.mcToken = mcToken;
            this.newRefreshToken = newRefreshToken;
            this.uuid = uuid;
            this.username = username;
        }

        public boolean isGood() {
            return mcToken != null;
        }
    }

    private static final String CLIENT_ID = "67b74668-ef33-49c3-a75c-18cbb2481e0c", CLIENT_SECRET = "hlQ8Q~33jTRilP4yE-UtuOt9wG.ZFLqq6pErIa2B";
    private static final int PORT = 3434;

    private static HttpServer server;
    private static Consumer<String> callback;

    private static void browse(final String url) {
        // set clipboard to url
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(url), null);
    }

    public static void getRefreshToken(final Consumer<String> callback) {
        MicrosoftLogin.callback = callback;

        startServer();
        browse("https://login.live.com/oauth20_authorize.srf?client_id=" + CLIENT_ID + "&response_type=code&redirect_uri=http://localhost:" + PORT + "/sad&display=touch&scope=XboxLive.signin%20offline_access");
    }

    private static final Gson gson = new Gson();

    public static LoginData login(String refreshToken) {
        // Refresh access token
        System.out.println("Trying get oauth token.");
        final AuthTokenResponse res = gson.fromJson(
                Browser.postExternal("https://login.live.com/oauth20_token.srf", "client_id=" + CLIENT_ID + "&refresh_token=" + refreshToken + "&grant_type=refresh_token&redirect_uri=http://localhost:" + PORT + "/sad", false),
                AuthTokenResponse.class
        );

        if (res == null) return null;

        final String accessToken = res.access_token;
        refreshToken = res.refresh_token;

        // XBL
        System.out.println("Trying get xbox token.");
        final XblXstsResponse xblRes = gson.fromJson(
                Browser.postExternal("https://user.auth.xboxlive.com/user/authenticate",
                        "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"d=" + accessToken + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}", true),
                XblXstsResponse.class);

        if (xblRes == null) return null;

        // XSTS
        System.out.println("Trying get xbox uhs.");
        final XblXstsResponse xstsRes = gson.fromJson(
                Browser.postExternal("https://xsts.auth.xboxlive.com/xsts/authorize",
                        "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + xblRes.Token + "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}", true),
                XblXstsResponse.class);

        if (xstsRes == null) return null;

        // Minecraft
        System.out.println("Trying get minecraft token.");
        final McResponse mcRes = gson.fromJson(
                Browser.postExternal("https://api.minecraftservices.com/authentication/login_with_xbox",
                        "{\"identityToken\":\"XBL3.0 x=" + xblRes.DisplayClaims.xui[0].uhs + ";" + xstsRes.Token + "\"}", true),
                McResponse.class);

        if (mcRes == null) return null;

        // Check game ownership
//        final GameOwnershipResponse gameOwnershipRes = gson.fromJson(
//                Browser.getBearerResponse("https://api.minecraftservices.com/entitlements/mcstore", mcRes.access_token),
//                GameOwnershipResponse.class);
//
//        if (gameOwnershipRes == null || !gameOwnershipRes.hasGameOwnership()) return new LoginData();

        // Profile
        System.out.println("Trying get minecraft profile.");
        final ProfileResponse profileRes = gson.fromJson(
                Browser.getBearerResponse("https://api.minecraftservices.com/minecraft/profile", mcRes.access_token),
                ProfileResponse.class);

        if (profileRes == null) return null;

        return new LoginData(mcRes.access_token, refreshToken, profileRes.id, profileRes.name);
    }

    private static void startServer() {
        if (server != null) return;

        try {
            server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);

            server.createContext("/sad", new Handler());
            server.setExecutor(executor);
            server.start();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static void stopServer() {
        if (server == null) return;

        server.stop(0);
        server = null;

        callback = null;
    }

    private static class Handler implements HttpHandler {
        @Override
        public void handle(final HttpExchange req) throws IOException {
            if (req.getRequestMethod().equals("GET")) {
                // Login
                final List<NameValuePair> query = URLEncodedUtils.parse(req.getRequestURI(), StandardCharsets.UTF_8.name());

                boolean ok = false;

                for (final NameValuePair pair : query) {
                    if (pair.getName().equals("code")) {
                        handleCode(pair.getValue());

                        ok = true;
                        break;
                    }
                }

                if (!ok) writeText(req, "登录失败");
                else writeText(req, "<html>登录成功您关闭此页面<script>close()</script></html>");
            }

            stopServer();
        }

        private void handleCode(final String code) {
            //System.out.println(code);
            final String response = Browser.postExternal("https://login.live.com/oauth20_token.srf",
                    "client_id=" + CLIENT_ID + "&code=" + code + "&grant_type=authorization_code&redirect_uri=http://localhost:" + PORT + "/sad", false);
            final AuthTokenResponse res = gson.fromJson(
                    response,
                    AuthTokenResponse.class);

            if (res == null) callback.accept(null);
            else callback.accept(res.refresh_token);
        }

        private void writeText(final HttpExchange req, final String text) throws IOException {
            final OutputStream out = req.getResponseBody();

            req.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            req.sendResponseHeaders(200, text.length());

            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        }
    }

    private static class AuthTokenResponse {
        @Expose
        @SerializedName("access_token")
        public String access_token;
        @Expose
        @SerializedName("refresh_token")
        public String refresh_token;
    }

    private static class XblXstsResponse {
        @Expose
        @SerializedName("Token")
        public String Token;
        @Expose
        @SerializedName("DisplayClaims")
        public DisplayClaims DisplayClaims;

        private static class DisplayClaims {
            @Expose
            @SerializedName("xui")
            private Claim[] xui;

            private static class Claim {
                @Expose
                @SerializedName("uhs")
                private String uhs;
            }
        }
    }

    private static class McResponse {
        @Expose
        @SerializedName("access_token")
        public String access_token;
    }

    private static class GameOwnershipResponse {
        @Expose
        @SerializedName("items")
        private Item[] items;

        private static class Item {
            @Expose
            @SerializedName("name")
            private String name;
        }

        private boolean hasGameOwnership() {
            boolean hasProduct = false;
            boolean hasGame = false;

            for (final Item item : items) {
                if (item.name.equals("product_minecraft")) hasProduct = true;
                else if (item.name.equals("game_minecraft")) hasGame = true;
            }

            return hasProduct && hasGame;
        }
    }

    private static class ProfileResponse {
        @Expose
        @SerializedName("id")
        public String id;
        @Expose
        @SerializedName("name")
        public String name;
    }
}