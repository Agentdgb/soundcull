# SoundCull (Minecraft 1.21 - 1.21.11)

A high-performance, client-side Fabric mod that eliminates audio stutter and OpenAL channel starvation by rate-limiting redundant ambient and mob sounds with zero-allocation concurrency.

## Why?
Minecraft has a hard limit of ~32 static OpenAL audio channels. In dense areas (villager trading halls, automatic mob farms, portal arrays), hundreds of identical sounds compete for audio channels every single second. This causes channel eviction loops, dropped game cues, and wasted CPU cycles on attenuation calculations.

SoundCull intercepts sound playback calls at the client entry point and drops excessive identical sounds before they ever reach OpenAL.

## Key Features
- **3D Spatial Pre-Culling:** Calculates distance falloff before calling OpenAL. Drops inaudible sounds outside hearing radius.
- **Proximity Safety (< 5 blocks):** Sounds originating within 5 blocks of the player are strictly immune to culling, ensuring nearby mobs in caves or combat are never silenced.
- **Lock-Free Concurrency:** Replaced synchronization blocks with atomic memory counters (`AtomicInteger`, `AtomicLong`), eliminating thread pauses in high-entity areas.
- **$O(1)$ Classification Cache:** Evaluates sound identifier prefixes once and caches decisions with zero GC allocations.
- **In-Game GUI Configuration:** Dedicated in-game visual settings screen with native sliders and toggles (`/soundcull gui`).
- **Critical Survival Immunity:** Creeper hissing, TNT fuses, Warden roars, player damage, and raid horns are strictly protected from culling.
- **Continuous Sound Protection:** `TickableSoundInstance` sounds (Elytra, Minecarts, Beacons) are fully protected from mid-playback cutoff.
- **Real-Time Telemetry:** Live chat feedback showing total sounds processed, filtered count, and estimated OpenAL channel savings (`/soundcull stats`).
- **Zero-Dependency Architecture:** Self-contained client mixins; does not force the heavy Fabric API as a required dependency.

## Commands (Client-Side)
- `/soundcull gui` (or `/soundcull config`) - Opens the visual configuration menu in-game.
- `/soundcull stats` - Displays live telemetry and OpenAL channel savings.
- `/soundcull toggle` - Enables or disables the mod on the fly.
- `/soundcull reset` - Resets telemetry counters and flushes the sound cache.
- `/soundcull` - Displays current configuration status.

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
