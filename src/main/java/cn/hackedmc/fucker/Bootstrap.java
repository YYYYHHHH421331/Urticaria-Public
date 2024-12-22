package cn.hackedmc.fucker;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.util.CryptUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.main.Main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;

public class Bootstrap {
    public static void main(final String[] args) {
        Main.main(concat(new String[]{"--version", Client.NAME, "--accessToken", "0", "--assetsDir", ".minecraft\\assets", "--assetIndex", "1.8", "--userProperties", "{}", "--gameDir", ".minecraft"}, args));
    }

    public static <T> T[] concat(final T[] first, final T[] second) {
        final T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
