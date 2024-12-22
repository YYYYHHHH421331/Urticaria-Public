package cn.hackedmc.urticaria.util.shader.base;

import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class RiseShader implements InstanceAccess {
    private boolean active;
    private boolean tryLessRender;

    public abstract void run(ShaderRenderType type, float partialTicks, List<Runnable> runnable);

    public abstract void update();
}
