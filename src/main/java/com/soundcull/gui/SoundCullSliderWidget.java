package com.soundcull.gui;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Function;

public class SoundCullSliderWidget extends SliderWidget {

    private final double min;
    private final double max;
    private final boolean integerOnly;
    private final Consumer<Double> onApply;
    private final Function<Double, Text> messageFactory;

    public SoundCullSliderWidget(int x, int y, int width, int height,
                                 double min, double max, double initialValue, boolean integerOnly,
                                 Function<Double, Text> messageFactory,
                                 Consumer<Double> onApply) {
        super(x, y, width, height, Text.empty(), clampNormalized((initialValue - min) / (max - min)));
        this.min = min;
        this.max = max;
        this.integerOnly = integerOnly;
        this.messageFactory = messageFactory;
        this.onApply = onApply;
        this.updateMessage();
    }

    private static double clampNormalized(double val) {
        return Math.max(0.0, Math.min(1.0, val));
    }

    public double getScaledValue() {
        double raw = min + this.value * (max - min);
        return integerOnly ? Math.round(raw) : raw;
    }

    @Override
    protected void updateMessage() {
        if (this.messageFactory != null) {
            this.setMessage(this.messageFactory.apply(getScaledValue()));
        }
    }

    @Override
    protected void applyValue() {
        if (this.onApply != null) {
            this.onApply.accept(getScaledValue());
        }
    }
}
