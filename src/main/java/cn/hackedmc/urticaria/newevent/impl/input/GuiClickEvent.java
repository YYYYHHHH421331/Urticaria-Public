package cn.hackedmc.urticaria.newevent.impl.input;

import cn.hackedmc.urticaria.newevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Patrick
 * @since 10/19/2021
 */
@Getter
@AllArgsConstructor
public final class GuiClickEvent extends CancellableEvent {
    private final int mouseX, mouseY, mouseButton;
}