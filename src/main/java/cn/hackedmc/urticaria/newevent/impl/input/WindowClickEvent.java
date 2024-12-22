package cn.hackedmc.urticaria.newevent.impl.input;

import cn.hackedmc.urticaria.newevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WindowClickEvent extends CancellableEvent {
    private final int windowId, slotId, mouseButton, mode;
}
