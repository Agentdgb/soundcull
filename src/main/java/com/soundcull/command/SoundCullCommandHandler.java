package com.soundcull.command;

import com.soundcull.SoundCullClient;
import com.soundcull.config.SoundCullConfig;
import com.soundcull.culling.SoundThrottler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class SoundCullCommandHandler {

    private SoundCullCommandHandler() {}

    public static boolean handleCommand(String rawText) {
        if (rawText == null) return false;
        String trimmed = rawText.trim();
        String lower = trimmed.toLowerCase();

        // Support both "/soundcull" and "soundcull"
        if (lower.startsWith("/")) {
            lower = lower.substring(1).trim();
        }

        if (!lower.equals("soundcull") && !lower.startsWith("soundcull ")) {
            return false;
        }

        SoundCullClient.trace("Command intercepted: " + rawText);
        MinecraftClient client = MinecraftClient.getInstance();

        String[] parts = lower.split("\\s+");
        if (parts.length == 1) {
            sendFeedback(client, Text.empty()
                    .append(Text.literal("[SoundCull v1.0.0] ").formatted(Formatting.GREEN, Formatting.BOLD))
                    .append(Text.literal("Estado: ").formatted(Formatting.GRAY))
                    .append(Text.literal(SoundCullConfig.enabled ? "ACTIVO" : "DESACTIVADO")
                            .formatted(SoundCullConfig.enabled ? Formatting.GREEN : Formatting.RED))
                    .append(Text.literal(" | Spatial: ").formatted(Formatting.GRAY))
                    .append(Text.literal(SoundCullConfig.spatialCulling ? "ON" : "OFF")
                            .formatted(SoundCullConfig.spatialCulling ? Formatting.GREEN : Formatting.RED))
                    .append(Text.literal(" | MinVolume: ").formatted(Formatting.GRAY))
                    .append(Text.literal(String.valueOf(SoundCullConfig.minVolumeThreshold)).formatted(Formatting.YELLOW))
                    .append(Text.literal(" | Ventana: ").formatted(Formatting.GRAY))
                    .append(Text.literal(SoundCullConfig.maxSoundsPerWindow + " sonidos / " + SoundCullConfig.windowMillis + "ms")
                            .formatted(Formatting.AQUA))
            );
            return true;
        }

        String sub = parts[1];
        switch (sub) {
            case "toggle" -> {
                SoundCullConfig.enabled = !SoundCullConfig.enabled;
                SoundCullConfig.save();
                sendFeedback(client, Text.empty()
                        .append(Text.literal("[SoundCull] ").formatted(Formatting.GREEN, Formatting.BOLD))
                        .append(Text.literal("Mod ").formatted(Formatting.GRAY))
                        .append(Text.literal(SoundCullConfig.enabled ? "ACTIVADO" : "DESACTIVADO")
                                .formatted(SoundCullConfig.enabled ? Formatting.GREEN : Formatting.RED))
                );
            }
            case "reset" -> {
                SoundThrottler.reset();
                sendFeedback(client, Text.empty()
                        .append(Text.literal("[SoundCull] ").formatted(Formatting.GREEN, Formatting.BOLD))
                        .append(Text.literal("Caché y estadísticas de frecuencias reiniciadas exitosamente.").formatted(Formatting.GRAY))
                );
            }
            case "stats" -> {
                long processed = SoundThrottler.TOTAL_PROCESSED.get();
                long culled = SoundThrottler.TOTAL_CULLED.get();
                double percent = processed > 0 ? (culled * 100.0 / processed) : 0.0;
                sendFeedback(client, Text.empty()
                        .append(Text.literal("[SoundCull Telemetry] ").formatted(Formatting.GREEN, Formatting.BOLD))
                        .append(Text.literal("Procesados: ").formatted(Formatting.GRAY))
                        .append(Text.literal(String.valueOf(processed)).formatted(Formatting.WHITE))
                        .append(Text.literal(" | Filtrados: ").formatted(Formatting.GRAY))
                        .append(Text.literal(String.valueOf(culled)).formatted(Formatting.YELLOW))
                        .append(Text.literal(String.format(" (%.1f%% canales ahorrados)", percent)).formatted(Formatting.AQUA))
                        .append(Text.literal(" | Trackers activos: ").formatted(Formatting.GRAY))
                        .append(Text.literal(String.valueOf(SoundThrottler.getActiveTrackersCount())).formatted(Formatting.WHITE))
                );
            }
            default -> sendFeedback(client, Text.empty()
                    .append(Text.literal("[SoundCull] ").formatted(Formatting.RED, Formatting.BOLD))
                    .append(Text.literal("Uso: /soundcull | /soundcull toggle | /soundcull stats | /soundcull reset")
                            .formatted(Formatting.GRAY))
            );
        }
        return true;
    }

    private static void sendFeedback(MinecraftClient client, Text message) {
        if (client.inGameHud != null && client.inGameHud.getChatHud() != null) {
            client.inGameHud.getChatHud().addMessage(message);
        }
    }
}
