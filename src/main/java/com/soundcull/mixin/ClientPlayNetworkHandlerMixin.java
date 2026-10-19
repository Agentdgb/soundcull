package com.soundcull.mixin;

import com.soundcull.SoundCullClient;
import com.soundcull.config.SoundCullConfig;
import com.soundcull.culling.SoundThrottler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "sendChatCommand(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void soundcull$onSendChatCommand(String command, CallbackInfo ci) {
        if (command == null) return;
        String trimmed = command.trim();
        String lower = trimmed.toLowerCase();

        if (lower.equals("soundcull") || lower.startsWith("soundcull ")) {
            ci.cancel();
            SoundCullClient.trace("ClientPlayNetworkHandler.sendChatCommand intercepted: " + command);

            MinecraftClient client = MinecraftClient.getInstance();
            String[] parts = lower.split("\\s+");
            if (parts.length == 1) {
                printFeedback(client, "§a[SoundCull v1.0.0] §7Status: §f" + (SoundCullConfig.enabled ? "§aACTIVE" : "§cDISABLED") +
                        " §7| Spatial: §f" + (SoundCullConfig.spatialCulling ? "§aON" : "§cOFF") +
                        " §7| MinVolume: §e" + SoundCullConfig.minVolumeThreshold +
                        " §7| Window: §b" + SoundCullConfig.maxSoundsPerWindow + " / " + SoundCullConfig.windowMillis + "ms");
                return;
            }

            String sub = parts[1];
            if (sub.equals("toggle")) {
                SoundCullConfig.enabled = !SoundCullConfig.enabled;
                SoundCullConfig.save();
                printFeedback(client, "§a[SoundCull] §7Mod " + (SoundCullConfig.enabled ? "§aACTIVADO" : "§cDESACTIVADO"));
            } else if (sub.equals("reset")) {
                SoundThrottler.reset();
                printFeedback(client, "§a[SoundCull] §7Cache de frecuencias de audio reiniciada.");
            } else {
                printFeedback(client, "§c[SoundCull] Uso: /soundcull | /soundcull toggle | /soundcull reset");
            }
        }
    }

    private void printFeedback(MinecraftClient client, String message) {
        if (client.inGameHud != null && client.inGameHud.getChatHud() != null) {
            client.inGameHud.getChatHud().addMessage(Text.literal(message));
        }
    }
}
