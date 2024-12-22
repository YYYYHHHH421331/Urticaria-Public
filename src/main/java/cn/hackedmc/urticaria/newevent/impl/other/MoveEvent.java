package cn.hackedmc.urticaria.newevent.impl.other;

import cn.hackedmc.urticaria.newevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class MoveEvent extends CancellableEvent {

    private double posX, posY, posZ;

    public void zeroXZ() {
        this.posX = this.posZ = 0;
    }

    public void zeroXYZ() {
        this.posX = this.posY = this.posZ = 0;
    }
}
