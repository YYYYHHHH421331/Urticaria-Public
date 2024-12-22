package cn.hackedmc.urticaria.newevent.impl.other;


import cn.hackedmc.urticaria.newevent.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;

@Getter
@AllArgsConstructor
public final class KillEvent implements Event {

    Entity entity;

}