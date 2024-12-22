package cn.hackedmc.urticaria.util;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class JsonUtil {
    @SerializedName("object")
    private final JsonObject object;

    public JsonUtil(JsonObject object) {
        this.object = object;
    }

    public String getString(String name, String defaultValue) {
        return object.has(name) ? object.get(name).getAsString() : defaultValue;
    }

    public long getLong(String name, long defaultValue) {
        return object.has(name) ? object.get(name).getAsLong() : defaultValue;
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        return object.has(name) ? object.get(name).getAsBoolean() : defaultValue;
    }

    public int getInt(String name, int defaultValue) {
        return object.has(name) ? object.get(name).getAsInt() : defaultValue;
    }
}
