package cn.hackedmc.urticaria.newevent.impl.input;

import cn.hackedmc.urticaria.newevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class MouseInputEvent extends CancellableEvent {
    private final int mouseX, mouseY, eventButton;
}
