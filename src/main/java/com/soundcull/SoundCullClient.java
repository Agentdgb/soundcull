package com.soundcull;

import com.soundcull.config.SoundCullConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

public final class SoundCullClient implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "soundcull";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static volatile boolean initialized = false;

    static {
        trace("SoundCullClient class static initializer loaded");
    }

    @Override
    public void onInitialize() {
        trace("SoundCullClient.onInitialize() [main entrypoint] called");
        init();
    }

    @Override
    public void onInitializeClient() {
        trace("SoundCullClient.onInitializeClient() [client entrypoint] called");
        init();
    }

    private static synchronized void init() {
        if (initialized) {
            trace("SoundCull already initialized, skipping duplicate init");
            return;
        }
        initialized = true;

        try {
            SoundCullConfig.load();
            trace("SoundCullConfig loaded. Enabled: " + SoundCullConfig.enabled + ", Spatial: " + SoundCullConfig.spatialCulling);
        } catch (Throwable t) {
            trace("CRITICAL: Failed to load SoundCullConfig: " + t.getMessage());
            t.printStackTrace();
        }
        LOGGER.info("[SoundCull] Initialized successfully.");
    }

    public static void trace(String msg) {
        String formatted = "[" + Instant.now() + "] " + msg;
        System.out.println("[SoundCull] " + msg);
        try {
            Path tracePath = Paths.get("C:/Users/Admin/AppData/Local/Programs/PrismLauncher/instances/1.21.11/minecraft/soundcull_trace.log");
            Files.writeString(tracePath, formatted + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Throwable ignored) {}
    }
}
