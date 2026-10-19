# SoundCull (Minecraft 1.21 - 1.21.11)

A lightweight Fabric mod that prevents audio engine stutter by rate-limiting redundant ambient and mob sounds.

## Why?
Minecraft has a hard limit of ~32 static OpenAL audio channels. In crowded areas (mob farms, villager trading halls), hundreds of identical sounds compete for these channels every second. This causes channel eviction loops and wasted CPU time on attenuation math.

SoundCull intercepts sound playback calls at the client entry point and drops excessive identical sounds before they reach OpenAL.

## Key Features
- **3D Spatial Pre-Culling:** Calculates distance falloff before calling OpenAL. Drops inaudible sounds outside hearing radius.
- **Rate-Limiting:** Caps repetitive mob/ambient sounds (cows, sheep, villagers, portals) to a configurable window (default: 3 per second).
- **Immunity List:** Critical gameplay cues (Creeper hissing, TNT fuse, Warden roars, player damage, raid horns) are never culled.
- **Continuous Sound Protection:** `TickableSoundInstance` sounds (Elytra, Minecarts, Beacons) are fully protected from mid-playback cutoff.
- **In-Game Command (`/soundcull`):** Real-time telemetry in chat showing exactly how many sounds were filtered and channels saved.
- **Zero-Allocation Pruning:** Automated memory management keeps RAM overhead below 30 KB.
- **Client-only:** Completely safe to use on any multiplayer server.

## Commands (Client-Side)
- `/soundcull` or `/soundcull stats` - Displays live telemetry and OpenAL channel savings.
- `/soundcull toggle` - Enables or disables the mod on the fly.
- `/soundcull reset` - Resets telemetry counters.

## Configuration
File: `config/soundcull.properties`
```properties
enabled=true
maxSoundsPerWindow=3
windowMillis=1000
minVolumeThreshold=0.02
spatialCulling=true
customSpamPrefixes=
```

## Compatibility
Fully compatible with Sodium, Lithium, FerriteCore, and Sound Physics Remastered.
