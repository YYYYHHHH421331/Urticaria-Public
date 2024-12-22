package cn.hackedmc.urticaria.module.impl.other;

import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.other.protocols.HuaYuTingProtocol;
import cn.hackedmc.urticaria.value.impl.ModeValue;

@ModuleInfo(name = "module.other.protocol.name", description = "module.other.protocol.description", category = Category.OTHER)
public class ServerProtocol extends Module {
    public static ServerProtocol INSTANCE;
    public HuaYuTingProtocol huaYuTingProtocol = new HuaYuTingProtocol("HuaYuTing", this);

    public final ModeValue mode = new ModeValue("Mode", this)
            .add(new HuaYuTingProtocol("HuaYuTing", this))
            .setDefault("HuaYuTing");

    public ServerProtocol() {
        INSTANCE = this;
    }
}
