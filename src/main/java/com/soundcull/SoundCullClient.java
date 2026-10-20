package com.soundcull;

import com.soundcull.config.SoundCullConfig;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SoundCullClient implements ClientModInitializer {
    public static final String MOD_ID = "soundcull";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static volatile boolean initialized = false;

    @Override
    public void onInitializeClient() {
        if (initialized) {
            return;
        }
        initialized = true;

        try {
            SoundCullConfig.load();
            LOGGER.info("[SoundCull] Loaded configuration (enabled={}, spatial={})",
                    SoundCullConfig.enabled, SoundCullConfig.spatialCulling);
        } catch (Throwable t) {
            LOGGER.error("[SoundCull] Failed to load configuration", t);
        }
        LOGGER.info("[SoundCull] Initialized successfully.");
    }
}
