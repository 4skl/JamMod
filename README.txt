JamMod brings synchronized multiplayer music and audio streaming directly into Minecraft.



Features:

- Zero-Drift Multiplayer Sync: Server-authoritative timestamps keep track positions aligned for all listeners.

- Direct Audio Streaming: Streams audio dynamically from external links (YouTube, SoundCloud, direct audio URLs) without bloated resource packs.

- Standalone Architecture: 100% self-contained. No external companion mods, native binary installations, or third-party authentication required.

- Late-Join Synchronization: Players logging in or joining a session automatically catch up to the current track position.

- Playback Controls: In-game interface for queuing, playing, pausing, skipping, and localized volume control.



Requirements & Installation:



  Client Side (Required):

  - Minecraft Forge (1.20.1 - 47.4.28+)

  - JamMod

  * Note: JamMod is completely standalone and bundles its own audio engine. Just drop it into your mods folder!



  Server Side (Required):

  - Minecraft Forge (1.20.1 - 47.4.28+)

  - JamMod

  * Note: The server only orchestrates room states, queues, and playback timestamps. It handles this entirely through standard Minecraft network packets (no extra ports need to be forwarded).