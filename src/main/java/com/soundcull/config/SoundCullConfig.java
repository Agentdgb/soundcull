package com.soundcull.config;

import com.soundcull.SoundCullClient;
import com.soundcull.culling.SoundThrottler;
import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public final class SoundCullConfig {
    public static boolean enabled = true;
    public static int maxSoundsPerWindow = 3;
    public static long windowMillis = 1000L;
    public static float minVolumeThreshold = 0.02f;
    public static boolean spatialCulling = true;

    private static final List<String> DEFAULT_SPAM = List.of(
            "entity.cow.", "entity.sheep.", "entity.pig.", "entity.chicken.",
            "entity.villager.", "entity.zombie_villager.", "entity.bat.",
            "entity.bee.", "entity.squid.", "entity.glow_squid.",
            "block.portal.ambient", "block.campfire.crackle",
            "block.water.ambient", "block.lava.ambient"
    );

    public static final Set<String> customSpamPrefixes = new HashSet<>(DEFAULT_SPAM);

    public static Path getConfigPath() {
        try {
            if (FabricLoader.getInstance() != null && FabricLoader.getInstance().getConfigDir() != null) {
                return FabricLoader.getInstance().getConfigDir().resolve("soundcull.properties");
            }
        } catch (Throwable t) {
            SoundCullClient.LOGGER.warn("[SoundCull] Could not resolve Fabric config dir, defaulting to config/", t);
        }
        return Paths.get("config/soundcull.properties");
    }

    public static void load() {
        try {
            Path configPath = getConfigPath();
            if (!Files.exists(configPath)) {
                save();
                return;
            }

            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(configPath)) {
                props.load(in);
                enabled = Boolean.parseBoolean(props.getProperty("enabled", "true"));
                maxSoundsPerWindow = Integer.parseInt(props.getProperty("maxSoundsPerWindow", "3"));
                windowMillis = Long.parseLong(props.getProperty("windowMillis", "1000"));
                minVolumeThreshold = Float.parseFloat(props.getProperty("minVolumeThreshold", "0.02"));
                spatialCulling = Boolean.parseBoolean(props.getProperty("spatialCulling", "true"));

                String custom = props.getProperty("customSpamPrefixes", "");
                if (!custom.isBlank()) {
                    for (String p : custom.split(",")) {
                        String trimmed = p.trim();
                        if (!trimmed.isEmpty()) {
                            customSpamPrefixes.add(trimmed);
                        }
                    }
                }
            }
            SoundThrottler.invalidateClassificationCache();
            SoundCullClient.LOGGER.debug("[SoundCull] Configuration loaded successfully.");
        } catch (Throwable t) {
            SoundCullClient.LOGGER.error("[SoundCull] Error loading configuration", t);
        }
    }

    public static void save() {
        Properties props = new Properties();
        props.setProperty("enabled", String.valueOf(enabled));
        props.setProperty("maxSoundsPerWindow", String.valueOf(maxSoundsPerWindow));
        props.setProperty("windowMillis", String.valueOf(windowMillis));
        props.setProperty("minVolumeThreshold", String.valueOf(minVolumeThreshold));
        props.setProperty("spatialCulling", String.valueOf(spatialCulling));
        props.setProperty("customSpamPrefixes", String.join(",", customSpamPrefixes));

        try {
            Path configPath = getConfigPath();
            if (configPath.getParent() != null) {
                Files.createDirectories(configPath.getParent());
            }
            try (OutputStream out = Files.newOutputStream(configPath)) {
                props.store(out, "SoundCull Configuration");
            }
            SoundThrottler.invalidateClassificationCache();
            SoundCullClient.LOGGER.debug("[SoundCull] Configuration saved successfully.");
        } catch (Throwable t) {
            SoundCullClient.LOGGER.error("[SoundCull] Error saving configuration", t);
        }
    }
}
