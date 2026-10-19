package com.soundcull;

import com.soundcull.config.SoundCullConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

public final class SoundCullClient implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "soundcull";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static volatile boolean initialized = false;
    private static Path logFile = null;

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
            LOGGER.error("[SoundCull] Failed to load config", t);
        }
        LOGGER.info("[SoundCull] Initialized successfully.");
    }

    public static void trace(String msg) {
        String formatted = "[" + Instant.now() + "] " + msg;
        LOGGER.debug("[SoundCull] {}", msg);
        try {
            if (logFile == null) {
                Path baseDir = FabricLoader.getInstance() != null && FabricLoader.getInstance().getGameDir() != null
                        ? FabricLoader.getInstance().getGameDir()
                        : Path.of(".");
                Path logsDir = baseDir.resolve("logs");
                if (!Files.exists(logsDir)) {
                    Files.createDirectories(logsDir);
                }
                logFile = logsDir.resolve("soundcull_trace.log");
            }
            Files.writeString(logFile, formatted + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Throwable ignored) {}
    }
}
