package cn.hackedmc.urticaria.newevent.impl.render;

import cn.hackedmc.urticaria.newevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.inventory.GuiContainer;

@Getter
@Setter
@AllArgsConstructor
public class ChestRenderEvent extends CancellableEvent {
    private final GuiContainer screen;
}
