package com.soundcull.mixin;

import com.soundcull.command.SoundCullCommandHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "sendMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    private void soundcull$onSendMessage(String chatText, boolean addToHistory, CallbackInfo ci) {
        if (chatText == null) return;

        if (SoundCullCommandHandler.handleCommand(chatText)) {
            ci.cancel();
            MinecraftClient client = MinecraftClient.getInstance();
            if (addToHistory && client.inGameHud != null) {
                client.inGameHud.getChatHud().addToMessageHistory(chatText);
            }
        }
    }
}
