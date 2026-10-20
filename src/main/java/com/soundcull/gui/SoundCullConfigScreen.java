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
        int startY = 45;
        int buttonWidth = 200;
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

        // 3. Cycle Max Sounds per Window (1 -> 2 -> 3 -> 5 -> 10)
        this.addDrawableChild(ButtonWidget.builder(
                getMaxSoundsText(),
                button -> {
                    int current = SoundCullConfig.maxSoundsPerWindow;
                    if (current <= 1) SoundCullConfig.maxSoundsPerWindow = 2;
                    else if (current == 2) SoundCullConfig.maxSoundsPerWindow = 3;
                    else if (current == 3) SoundCullConfig.maxSoundsPerWindow = 5;
                    else if (current == 5) SoundCullConfig.maxSoundsPerWindow = 10;
                    else SoundCullConfig.maxSoundsPerWindow = 1;
                    SoundCullConfig.save();
                    button.setMessage(getMaxSoundsText());
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight).build());

        // 4. Cycle Window Time (500ms -> 1000ms -> 1500ms -> 2000ms)
        this.addDrawableChild(ButtonWidget.builder(
                getWindowMillisText(),
                button -> {
                    long current = SoundCullConfig.windowMillis;
                    if (current <= 500L) SoundCullConfig.windowMillis = 1000L;
                    else if (current == 1000L) SoundCullConfig.windowMillis = 1500L;
                    else if (current == 1500L) SoundCullConfig.windowMillis = 2000L;
                    else SoundCullConfig.windowMillis = 500L;
                    SoundCullConfig.save();
                    button.setMessage(getWindowMillisText());
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight).build());

        // 5. Reset Stats & Cache
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Reiniciar Telemetría / Caché").formatted(Formatting.YELLOW),
                button -> {
                    SoundThrottler.reset();
                    button.setMessage(Text.literal("¡Reiniciado!").formatted(Formatting.GREEN));
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 4, buttonWidth, buttonHeight).build());

        // 6. Back / Done button
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

    private Text getMaxSoundsText() {
        return Text.empty()
                .append(Text.literal("Límite de Sonidos: ").formatted(Formatting.GRAY))
                .append(Text.literal(SoundCullConfig.maxSoundsPerWindow + " por ventana")
                        .formatted(Formatting.AQUA));
    }

    private Text getWindowMillisText() {
        return Text.empty()
                .append(Text.literal("Duración Ventana: ").formatted(Formatting.GRAY))
                .append(Text.literal(SoundCullConfig.windowMillis + " ms")
                        .formatted(Formatting.AQUA));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Header Title
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("SoundCull - Ajustes de Optimización de Audio").formatted(Formatting.GREEN, Formatting.BOLD),
                this.width / 2,
                18,
                0xFFFFFF
        );

        // Footer Telemetry hint
        long processed = SoundThrottler.TOTAL_PROCESSED.get();
        long culled = SoundThrottler.TOTAL_CULLED.get();
        double percent = processed > 0 ? (culled * 100.0 / processed) : 0.0;
        String statsText = String.format("Ahorro actual: %.1f%% de canales | Filtrados: %d", percent, culled);
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(statsText).formatted(Formatting.DARK_GRAY),
                this.width / 2,
                this.height - 25,
                0x888888
        );
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
