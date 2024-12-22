package cn.hackedmc.urticaria.component.impl.viamcp;

import cn.hackedmc.urticaria.component.Component;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.render.MouseOverEvent;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.viamcp.ViaMCP;

public final class HitboxFixComponent extends Component {

    @EventLink()
    public final Listener<MouseOverEvent> onMouseOver = event -> {

        if (ViaMCP.getInstance().getVersion() > ProtocolVersion.v1_8.getVersion()) {
            event.setExpand(event.getExpand() - 0.1f);
        }
    };
}
