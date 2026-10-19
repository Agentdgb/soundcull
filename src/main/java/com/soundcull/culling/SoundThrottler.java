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
import java.util.concurrent.atomic.AtomicLong;

public final class SoundThrottler {
    private static final Map<String, WindowTracker> TRACKERS = new ConcurrentHashMap<>();
    private static final AtomicLong LAST_CLEANUP = new AtomicLong(0L);

    // Live Telemetry Counters
    public static final AtomicLong TOTAL_PROCESSED = new AtomicLong(0L);
    public static final AtomicLong TOTAL_CULLED = new AtomicLong(0L);

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

    private static final class WindowTracker {
        long windowStart;
        int count;

        WindowTracker(long start) {
            this.windowStart = start;
            this.count = 1;
        }
    }

    public static boolean shouldCull(SoundInstance sound) {
        if (!SoundCullConfig.enabled || sound == null) return false;

        TOTAL_PROCESSED.incrementAndGet();

        // 1. Tickable sounds (Elytra, Minecarts, Beacons) are never throttled
        if (sound instanceof TickableSoundInstance) {
            return false;
        }

        Identifier id = sound.getId();
        if (id == null) return false;

        String path = id.getPath();

        // 2. Critical survival immunity
        for (String crit : CRITICAL_PREFIXES) {
            if (path.startsWith(crit)) return false;
        }

        // 3. True 3D Spatial Attenuation Culling
        if (SoundCullConfig.spatialCulling && sound.getAttenuationType() != SoundInstance.AttenuationType.NONE) {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;

            if (player != null) {
                double dx = sound.getX() - player.getX();
                double dy = sound.getY() - player.getY();
                double dz = sound.getZ() - player.getZ();
                double distSq = dx * dx + dy * dy + dz * dz;

                // Max hearing radius (default falloff in Minecraft is 16 blocks * sound volume)
                float nominalVolume = Math.max(1.0f, sound.getVolume());
                double maxRange = nominalVolume * 16.0;

                // If sound source is outside audible sphere, drop before OpenAL processing
                if (distSq > (maxRange * maxRange)) {
                    TOTAL_CULLED.incrementAndGet();
                    return true;
                }
            }
        }

        // 4. Rate-Limiting for Spam Categories
        if (isThrottledCategory(path)) {
            long now = System.currentTimeMillis();
            pruneStaleTrackersIfNeeded(now);

            WindowTracker tracker = TRACKERS.computeIfAbsent(path, k -> new WindowTracker(now));

            synchronized (tracker) {
                if (now - tracker.windowStart > SoundCullConfig.windowMillis) {
                    tracker.windowStart = now;
                    tracker.count = 1;
                    return false;
                }

                if (tracker.count < SoundCullConfig.maxSoundsPerWindow) {
                    tracker.count++;
                    return false;
                }

                TOTAL_CULLED.incrementAndGet();
                return true;
            }
        }

        return false;
    }

    private static boolean isThrottledCategory(String path) {
        for (String prefix : SoundCullConfig.customSpamPrefixes) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }

    /**
     * Periodically clears idle trackers to prevent memory leaks on long play sessions.
     */
    private static void pruneStaleTrackersIfNeeded(long now) {
        long last = LAST_CLEANUP.get();
        if (now - last > 15000L && LAST_CLEANUP.compareAndSet(last, now)) {
            TRACKERS.entrySet().removeIf(entry -> now - entry.getValue().windowStart > 10000L);
        }
    }

    public static void resetStats() {
        TOTAL_PROCESSED.set(0L);
        TOTAL_CULLED.set(0L);
        TRACKERS.clear();
    }

    public static void reset() {
        resetStats();
    }
}
