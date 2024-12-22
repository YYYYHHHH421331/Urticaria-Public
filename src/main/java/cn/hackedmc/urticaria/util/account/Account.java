package cn.hackedmc.urticaria.util.account;

import cn.hackedmc.urticaria.util.SkinUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {
    private final String type;
    private final String email;
    private String password;
    private String username;
    private String uuid;
    private String refreshToken;

    public Account(final String email, final String password) {
        this.email = email;
        this.password = password;
        this.username = SkinUtil.uuidOf("Steve");
        this.type = "Offline";
    }

    public Account(final String cookie) {
        this.email = cookie;
        this.username = cookie;
        this.type = "Netease";
    }

    public Account(String email, String password, String username, String uuid, String refreshToken) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.uuid = uuid;
        this.refreshToken = refreshToken;
        this.type = "Microsoft";
    }

    public void setUsername(String username) {
        this.username = username;
        this.uuid = SkinUtil.uuidOf(username);
    }

    public void setOfflineUsername(String username) {
        this.username = username;
        this.uuid = SkinUtil.uuidOf("Steve");
    }
}
