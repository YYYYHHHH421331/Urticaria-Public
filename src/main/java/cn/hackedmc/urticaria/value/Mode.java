package cn.hackedmc.urticaria.value;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.interfaces.Toggleable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Rewritten from Patricks old version to be less retarded
 * @author Hazsi
 * @since 10/10/2022
 */
@Getter
@RequiredArgsConstructor
public abstract class Mode<T> implements InstanceAccess, Toggleable {
    private final String name;
    private final T parent;
    private final List<Value<?>> values = new ArrayList<>();

    public final void register() {
        Client.INSTANCE.getEventBus().register(this);
        this.onEnable();
    }

    public final void unregister() {
        Client.INSTANCE.getEventBus().unregister(this);
        this.onDisable();
    }

    @Override
    public void toggle() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }
}