package cn.hackedmc.urticaria.ui.click.standard.components.value.impl;

import cn.hackedmc.urticaria.util.gui.GUIUtil;
import cn.hackedmc.urticaria.ui.click.standard.components.value.ValueComponent;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.Value;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import lombok.Getter;

@Getter
public class ModeValueComponent extends ValueComponent {

    public ModeValueComponent(final Value<?> value) {
        super(value);
    }

    @Override
    public void draw(final Vector2d position, final int mouseX, final int mouseY, final float partialTicks) {
        final ModeValue modeValue = (ModeValue) value;
        this.position = position;

        final String prefix = this.value.getName() + ":";

        this.nunitoSmall.drawString(prefix, this.position.x, this.position.y, this.getStandardClickGUI().fontDarkColor.hashCode());
        this.nunitoSmall.drawString(modeValue.getValue().getName(), this.position.x + this.nunitoSmall.width(prefix) + 2, this.position.y, this.getStandardClickGUI().fontDarkColor.hashCode());
    }

    @Override
    public void click(final int mouseX, final int mouseY, final int mouseButton) {
        if (this.position == null) {
            return;
        }

        final ModeValue modeValue = (ModeValue) value;

        final boolean left = mouseButton == 0;
        final boolean right = mouseButton == 1;

        if (GUIUtil.mouseOver(this.position.x, this.position.y - 3.5f, getStandardClickGUI().width - 70, this.height, mouseX, mouseY)) {
            final int currentIndex = modeValue.getModes().indexOf(modeValue.getValue());

            Mode<?> mode = null;
            if (left) {
                if (modeValue.getModes().size() <= currentIndex + 1) {
                    mode = modeValue.getModes().get(0);
                } else {
                    mode = modeValue.getModes().get(currentIndex + 1);
                }
            } else if (right) {
                if (0 > currentIndex - 1) {
                    mode = modeValue.getModes().get(modeValue.getModes().size() - 1);
                } else {
                    mode = modeValue.getModes().get(currentIndex - 1);
                }
            }

            if (mode != null) {
                modeValue.update(mode);
            }
        }
    }

    @Override
    public void released() {

    }

    @Override
    public void bloom() {

    }

    @Override
    public void key(final char typedChar, final int keyCode) {

    }
}

