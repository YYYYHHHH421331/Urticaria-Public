package cn.hackedmc.urticaria.module.impl.player;


import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.player.antivoid.*;
import cn.hackedmc.urticaria.value.impl.ModeValue;


/**
 * @author Alan
 * @since 23/10/2021
 */

@Rise
@ModuleInfo(name = "module.player.antivoid.name", description = "module.player.antivoid.description", category = Category.PLAYER)
public class AntiVoid extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new PacketAntiVoid("Packet", this))
            .add(new PositionAntiVoid("Position", this))
            .add(new BlinkAntiVoid("Blink", this))
            .add(new BlinkAntiVoid("Watchdog", this))
            .add(new FreezeAntiVoid("Freeze", this))
            .add(new VulcanAntiVoid("Vulcan", this))
            .add(new CollisionAntiVoid("Collision", this))
            .setDefault("Packet");
}