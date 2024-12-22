package cn.hackedmc.urticaria.module.impl.render.esp;

import cn.hackedmc.urticaria.component.impl.render.ESPComponent;
import cn.hackedmc.urticaria.component.impl.render.espcomponent.api.ESPColor;
import cn.hackedmc.urticaria.component.impl.render.espcomponent.impl.PlayerGlow;
import cn.hackedmc.urticaria.module.impl.render.ESP;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.value.Mode;

import java.awt.*;

public final class GlowESP extends Mode<ESP> {

    public GlowESP(String name, ESP parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        Color color = getTheme().getFirstColor();
        ESPComponent.add(new PlayerGlow(new ESPColor(color, color, color)));
    };
}