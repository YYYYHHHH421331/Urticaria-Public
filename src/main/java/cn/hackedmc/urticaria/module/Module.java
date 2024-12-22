package cn.hackedmc.urticaria.module;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.component.impl.render.notificationcomponent.Notification;
import cn.hackedmc.urticaria.component.impl.render.notificationcomponent.NotificationComponent;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.render.ClickGUI;
import cn.hackedmc.urticaria.module.impl.render.Interface;
import cn.hackedmc.urticaria.newevent.impl.other.ModuleToggleEvent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.localization.Localization;
import cn.hackedmc.urticaria.util.sound.SoundUtil;
import cn.hackedmc.urticaria.value.Value;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrick
 * @since 10/19/2021
 */
@Getter
@Setter
public abstract class Module implements InstanceAccess {

    private final String displayName;
    private final List<Value<?>> values = new ArrayList<>();
    private ModuleInfo moduleInfo;
    private boolean hidden;
    private boolean enabled;
    private int keyCode;
    public Module() {
        if (this.getClass().isAnnotationPresent(ModuleInfo.class)) {
            this.moduleInfo = this.getClass().getAnnotation(ModuleInfo.class);

            this.displayName = this.moduleInfo.name();
            this.keyCode = this.moduleInfo.keyBind();
            this.hidden = moduleInfo.hidden();
        } else {
            throw new RuntimeException("ModuleInfo annotation not found on " + this.getClass().getSimpleName());
        }
    }

    public Module(final ModuleInfo info) {
        this.moduleInfo = info;

        this.displayName = this.moduleInfo.name();
        this.keyCode = this.moduleInfo.keyBind();
    }

    public void toggle() {
        this.setEnabled(!enabled);
    }

    public void setEnabled(final boolean enabled) {
        if (this.enabled == enabled || (!this.moduleInfo.allowDisable() && !enabled)) {
            return;
        }

        this.enabled = enabled;

        Client.INSTANCE.getEventBus().handle(new ModuleToggleEvent(this));

        if (!Client.INSTANCE.noNotify && Interface.INSTANCE.showToggle.getValue() && !Interface.INSTANCE.notifyMode.getValue().getName().equalsIgnoreCase("Off")) {
            NotificationComponent.post(Localization.get(this.displayName), enabled ? Localization.get("notification.enable") : Localization.get("notification.disable"), enabled ? Notification.Type.SUCCESS : Notification.Type.ERROR, 1000L);
        }
        SoundUtil.toggleSound(enabled);

        if (enabled) {
            superEnable();
        } else {
            superDisable();
        }
    }

    /**
     * Called when a module gets enabled
     * -> important: whenever you override this method in a subclass
     * keep the super.onEnable()
     */
    public final void superEnable() {
        Client.INSTANCE.getEventBus().register(this);

        this.values.stream()
                .filter(value -> value instanceof ModeValue)
                .forEach(value -> ((ModeValue) value).getValue().register());

        this.values.stream()
                .filter(value -> value instanceof BooleanValue)
                .forEach(value -> {
                    final BooleanValue booleanValue = (BooleanValue) value;
                    if (booleanValue.getMode() != null && booleanValue.getValue()) {
                        booleanValue.getMode().register();
                    }
                });

        if (mc.thePlayer != null) this.onEnable();
    }

    /**
     * Called when a module gets disabled
     * -> important: whenever you override this method in a subclass
     * keep the super.onDisable()
     */
    public final void superDisable() {
        Client.INSTANCE.getEventBus().unregister(this);

        this.values.stream()
                .filter(value -> value instanceof ModeValue)
                .forEach(value -> ((ModeValue) value).getValue().unregister());

        this.values.stream()
                .filter(value -> value instanceof BooleanValue)
                .forEach(value -> {
                    final BooleanValue booleanValue = (BooleanValue) value;
                    if (booleanValue.getMode() != null) {
                        booleanValue.getMode().unregister();
                    }
                });

        if (mc.thePlayer != null) this.onDisable();
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public List<Value<?>> getAllValues() {
        ArrayList<Value<?>> allValues = new ArrayList<>();

        values.forEach(value -> {
            List<Value<?>> subValues = value.getSubValues();

            allValues.add(value);

            if (subValues != null) {
                allValues.addAll(subValues);
            }
        });

        return allValues;
    }

    public boolean shouldDisplay(Interface instance) {
        if (this instanceof ClickGUI) return false;
        if (!this.getModuleInfo().allowDisable()) return false;

        switch (instance.getModulesToShow().getValue().getName()) {
            case "All": {
                return true;
            }
            case "Exclude render": {
                return !this.getModuleInfo().category().equals(Category.RENDER);
            }
            case "Only bound": {
                return this.getKeyCode() != Keyboard.KEY_NONE;
            }
        }
        return true;
    }
}