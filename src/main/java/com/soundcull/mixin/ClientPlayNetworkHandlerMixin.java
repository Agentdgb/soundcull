package com.soundcull.mixin;

import com.soundcull.command.SoundCullCommandHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "sendChatCommand(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void soundcull$onSendChatCommand(String command, CallbackInfo ci) {
        if (command == null) return;

        if (SoundCullCommandHandler.handleCommand(command)) {
            ci.cancel();
        }
    }
}
