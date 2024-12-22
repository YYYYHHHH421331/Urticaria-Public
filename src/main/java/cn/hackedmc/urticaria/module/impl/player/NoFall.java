package cn.hackedmc.urticaria.module.impl.player;


import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.player.nofall.*;
import cn.hackedmc.urticaria.value.impl.ModeValue;


/**
 * @author Alan
 * @since 23/10/2021
 */

@Rise
@ModuleInfo(name = "module.player.nofall.name", description = "module.player.nofall.description", category = Category.PLAYER)
public class NoFall extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SpoofNoFall("Spoof", this))
            .add(new NoGroundNoFall("No Ground", this))
            .add(new RoundNoFall("Round", this))
            .add(new PlaceNoFall("Place", this))
            .add(new PacketNoFall("Packet", this))
            .add(new InvalidNoFall("Invalid", this))
            .add(new LagNoFall("Lag", this))
            .add(new ChunkLoadNoFall("Chunk Load", this))
            .add(new ClutchNoFall("Clutch", this))
            .add(new MatrixNoFall("Matrix", this))
            .add(new WatchdogNoFall("Watchdog", this))
            .setDefault("Spoof");
}
