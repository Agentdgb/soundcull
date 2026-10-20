package com.soundcull.gui;

import com.soundcull.config.SoundCullConfig;
import com.soundcull.culling.SoundThrottler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SoundCullConfigScreen extends Screen {

    private final Screen parent;

    public SoundCullConfigScreen(Screen parent) {
        super(Text.literal("Configuración de SoundCull"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int buttonWidth = 220;
        int buttonHeight = 20;
        int spacing = 24;

        // 1. Toggle Mod Enable/Disable
        this.addDrawableChild(ButtonWidget.builder(
                getModEnabledText(),
                button -> {
                    SoundCullConfig.enabled = !SoundCullConfig.enabled;
                    SoundCullConfig.save();
                    button.setMessage(getModEnabledText());
                }
        ).dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

        // 2. Toggle Spatial Culling
        this.addDrawableChild(ButtonWidget.builder(
                getSpatialCullingText(),
                button -> {
                    SoundCullConfig.spatialCulling = !SoundCullConfig.spatialCulling;
                    SoundCullConfig.save();
                    button.setMessage(getSpatialCullingText());
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build());

        // 3. Draggable Slider: Max Sounds per Window (1 to 15)
        this.addDrawableChild(new SoundCullSliderWidget(
                centerX - buttonWidth / 2,
                startY + spacing * 2,
                buttonWidth,
                buttonHeight,
                1.0,
                15.0,
                SoundCullConfig.maxSoundsPerWindow,
                true,
                val -> Text.empty()
                        .append(Text.literal("Límite de Sonidos: ").formatted(Formatting.GRAY))
                        .append(Text.literal(String.format("%.0f", val) + " / ventana").formatted(Formatting.AQUA)),
                val -> {
                    SoundCullConfig.maxSoundsPerWindow = (int) Math.round(val);
                    SoundCullConfig.save();
                }
        ));

        // 4. Draggable Slider: Window Millis (200ms to 3000ms)
        this.addDrawableChild(new SoundCullSliderWidget(
                centerX - buttonWidth / 2,
                startY + spacing * 3,
                buttonWidth,
                buttonHeight,
                200.0,
                3000.0,
                SoundCullConfig.windowMillis,
                true,
                val -> Text.empty()
                        .append(Text.literal("Duración Ventana: ").formatted(Formatting.GRAY))
                        .append(Text.literal(String.format("%.0f", val) + " ms").formatted(Formatting.AQUA)),
                val -> {
                    SoundCullConfig.windowMillis = (long) Math.round(val);
                    SoundCullConfig.save();
                }
        ));

        // 5. Reset Stats & Cache Button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Reiniciar Telemetría / Caché").formatted(Formatting.YELLOW),
                button -> {
                    SoundThrottler.reset();
                    button.setMessage(Text.literal("¡Reiniciado con éxito!").formatted(Formatting.GREEN));
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 4 + 4, buttonWidth, buttonHeight).build());

        // 6. Save & Return Button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Guardar y Volver").formatted(Formatting.WHITE),
                button -> this.close()
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 6, buttonWidth, buttonHeight).build());
    }

    private Text getModEnabledText() {
        return Text.empty()
                .append(Text.literal("Estado del Mod: ").formatted(Formatting.GRAY))
                .append(Text.literal(SoundCullConfig.enabled ? "ACTIVADO" : "DESACTIVADO")
                        .formatted(SoundCullConfig.enabled ? Formatting.GREEN : Formatting.RED));
    }

    private Text getSpatialCullingText() {
        return Text.empty()
                .append(Text.literal("Culling Espacial 3D: ").formatted(Formatting.GRAY))
                .append(Text.literal(SoundCullConfig.spatialCulling ? "ACTIVADO" : "DESACTIVADO")
                        .formatted(SoundCullConfig.spatialCulling ? Formatting.GREEN : Formatting.RED));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Header Title
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("SoundCull — Ajustes de Audio").formatted(Formatting.GREEN, Formatting.BOLD),
                this.width / 2,
                16,
                0xFFFFFF
        );

        // Subtitle / Hint
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Inmunidad de Proximidad: Activa (< 5 bloques nunca se silencian)").formatted(Formatting.DARK_GREEN),
                this.width / 2,
                28,
                0x55FF55
        );

        // Telemetry Footer
        long processed = SoundThrottler.TOTAL_PROCESSED.get();
        long culled = SoundThrottler.TOTAL_CULLED.get();
        double percent = processed > 0 ? (culled * 100.0 / processed) : 0.0;
        String statsText = String.format("Ahorro actual: %.1f%% de canales OpenAL | Descartados: %d", percent, culled);
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(statsText).formatted(Formatting.GRAY),
                this.width / 2,
                this.height - 22,
                0xAAAAAA
        );
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
