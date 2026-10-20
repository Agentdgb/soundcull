package com.soundcull.culling;

import com.soundcull.config.SoundCullConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class SoundThrottler {
    private static final Map<String, WindowTracker> TRACKERS = new ConcurrentHashMap<>();
    private static final Map<String, SoundClassification> CLASSIFICATION_CACHE = new ConcurrentHashMap<>();
    private static final AtomicLong LAST_CLEANUP = new AtomicLong(0L);

    // Live Telemetry Counters
    public static final AtomicLong TOTAL_PROCESSED = new AtomicLong(0L);
    public static final AtomicLong TOTAL_CULLED = new AtomicLong(0L);

    // Proximity immunity threshold (blocks squared: 5.0 * 5.0 = 25.0)
    // Any mob or sound within 5 blocks of the player is never culled.
    public static final double PROXIMITY_IMMUNITY_SQ = 25.0;

    private static final Set<String> CRITICAL_PREFIXES = Set.of(
            "entity.creeper.primed",
            "entity.tnt.primed",
            "entity.warden.",
            "entity.player.hurt",
            "entity.player.death",
            "entity.generic.explode",
            "event.raid.horn",
            "block.bell.use",
            "entity.wither.",
            "entity.ender_dragon."
    );

    private enum SoundClassification {
        IMMUNE,
        THROTTLED,
        NORMAL
    }

    private static final class WindowTracker {
        final AtomicLong windowStart;
        final AtomicInteger count;

        WindowTracker(long start) {
            this.windowStart = new AtomicLong(start);
            this.count = new AtomicInteger(1);
        }

        boolean allow(long now, long windowMillis, int maxSounds) {
            long start = windowStart.get();
            if (now - start > windowMillis) {
                if (windowStart.compareAndSet(start, now)) {
                    count.set(1);
                    return true;
                }
            }
            return count.incrementAndGet() <= maxSounds;
        }
    }

    public static boolean shouldCull(SoundInstance sound) {
        if (!SoundCullConfig.enabled || sound == null) return false;

        TOTAL_PROCESSED.incrementAndGet();

        // 1. Continuous/Tickable sounds (Elytra, Minecarts, Beacons) are never throttled
        if (sound instanceof TickableSoundInstance) {
            return false;
        }

        Identifier id = sound.getId();
        if (id == null) return false;

        String path = id.getPath();

        // 2. High-speed cached classification check (O(1) after first lookup)
        SoundClassification classification = CLASSIFICATION_CACHE.computeIfAbsent(path, SoundThrottler::classify);
        if (classification == SoundClassification.IMMUNE) {
            return false;
        }

        // 3. Distance check: Proximity Immunity and 3D Spatial Attenuation Culling
        if (sound.getAttenuationType() != SoundInstance.AttenuationType.NONE) {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;

            if (player != null) {
                double dx = sound.getX() - player.getX();
                double dy = sound.getY() - player.getY();
                double dz = sound.getZ() - player.getZ();
                double distSq = dx * dx + dy * dy + dz * dz;

                // A. Proximity Immunity: Sounds within 5 blocks are NEVER silenced (caves, close combat, direct proximity)
                if (distSq <= PROXIMITY_IMMUNITY_SQ) {
                    return false;
                }

                // B. Spatial pre-culling outside hearing radius (default 16 blocks * nominal volume)
                if (SoundCullConfig.spatialCulling) {
                    float nominalVolume = Math.max(1.0f, sound.getVolume());
                    double maxRange = nominalVolume * 16.0;

                    if (distSq > (maxRange * maxRange)) {
                        TOTAL_CULLED.incrementAndGet();
                        return true;
                    }
                }
            }
        }

        // 4. Rate-Limiting for Spam Categories (lock-free)
        if (classification == SoundClassification.THROTTLED) {
            long now = System.currentTimeMillis();
            pruneStaleTrackersIfNeeded(now);

            WindowTracker tracker = TRACKERS.computeIfAbsent(path, k -> new WindowTracker(now));

            if (!tracker.allow(now, SoundCullConfig.windowMillis, SoundCullConfig.maxSoundsPerWindow)) {
                TOTAL_CULLED.incrementAndGet();
                return true;
            }
        }

        return false;
    }

    private static SoundClassification classify(String path) {
        for (String crit : CRITICAL_PREFIXES) {
            if (path.startsWith(crit)) {
                return SoundClassification.IMMUNE;
            }
        }
        for (String prefix : SoundCullConfig.customSpamPrefixes) {
            if (path.startsWith(prefix)) {
                return SoundClassification.THROTTLED;
            }
        }
        return SoundClassification.NORMAL;
    }

    /**
     * Invalidate caches when configuration is modified or reloaded.
     */
    public static void invalidateClassificationCache() {
        CLASSIFICATION_CACHE.clear();
    }

    public static int getActiveTrackersCount() {
        return TRACKERS.size();
    }

    /**
     * Periodically clears idle trackers to prevent memory leaks on long play sessions.
     */
    private static void pruneStaleTrackersIfNeeded(long now) {
        long last = LAST_CLEANUP.get();
        if (now - last > 15000L && LAST_CLEANUP.compareAndSet(last, now)) {
            TRACKERS.entrySet().removeIf(entry -> now - entry.getValue().windowStart.get() > 10000L);
        }
    }

    public static void resetStats() {
        TOTAL_PROCESSED.set(0L);
        TOTAL_CULLED.set(0L);
        TRACKERS.clear();
        CLASSIFICATION_CACHE.clear();
    }

    public static void reset() {
        resetStats();
    }
}
