package cn.hackedmc.urticaria.newevent.impl.inventory;


import cn.hackedmc.urticaria.newevent.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SyncCurrentItemEvent implements Event {
    private int slot;
}