# JamMod

JamMod brings synchronized multiplayer music and audio streaming directly into Minecraft.

---

### Supported Versions & Loaders

| Minecraft Version | Mod Loader | Minimum Loader Version |
| :--- | :--- | :--- |
| **1.18.2** | Forge | 40.2.0+ |
| **1.19.2** | Forge | 43.3.0+ |
| **1.19.4** | Forge | 45.2.0+ |
| **1.20.1** | Forge | 47.1.0+ |
| **1.20.4** | NeoForge | 20.4.190+ |
| **1.21.1** | NeoForge | 21.1.65+ |

---

### Features

- **Zero-Drift Multiplayer Sync:** Server-authoritative timestamps keep track positions aligned for all listeners down to the millisecond. Pausing, resuming, and late-joining automatically align playback without audio drift.
- **Persistent Synced Jams:** Every room acts as a live, persistent playlist. Create a room, add media, and anyone in the session hears the same stream simultaneously.
- **Public & Private Rooms:** Host Public Jams that appear in the server directory, or Private Jams restricted to whitelisted members.
- **Tiered Role & Member Management:**
  - **Host (Owner):** Full control over room privacy, deletion, edit policies, and member roles.
  - **Admin:** Can cycle edit policies, invite players, manage roles (Editor/Listener), and remove lower-tier members.
  - **Editor:** Can add, remove, and reorder tracks when the policy is set to *Collaborators* or *Open to All*.
  - **Listener:** Streams in sync with the room.
  - **Offline Member Support:** Manage disconnected players or register offline users by username.
- **Configurable HUD Mini-Player:**
  - Unobtrusive animated overlay displaying track title and icon when a song begins.
  - Configurable in-game: toggle on/off and choose between four screen corners (`Top-Right`, `Top-Left`, `Bottom-Right`, `Bottom-Left`).
  - Preferences persist locally in `config/jammod_client.json`.
  - Display timer freezes while menus are open so track alerts are not missed.
- **Timeline Scrubbing & Seekbar:** Click directly on the timeline bar in the GUI or use `/jam seek <seconds>` to jump to any point in the track across all listeners.
- **Queue Modes & Reordering:**
  - Loop All (`🔁`), Loop Single Track (`🔂`), and Shuffle (`🔀`).
  - Move tracks up or down (`▲` / `▼`) in the queue.
  - Metadata (title and duration) resolves automatically via Lavaplayer.
- **Vanilla Music Volume Binding:** Linked directly to Minecraft's native `Music` volume slider via an in-GUI slider or the `/jam volume` command.
- **Clickable Chat Links & Anti-Spam:** Room invites and directory listings feature clickable text to join rooms with one click. Built-in rate limits prevent spam, and players can opt out of invitations with `/jam toggleinvites`.
- **Bandwidth Saver:** When all players leave a Jam, playback automatically idles to conserve server and network resources.
- **Standalone Architecture:** Completely self-contained with no companion mods, external servers, or native binary installations required.

---

### Installation

#### Client Side (Required):
1. Install the appropriate mod loader for your Minecraft version (Forge or NeoForge).
2. Drop the corresponding `JamMod-<loader>-<version>.jar` into your `.minecraft/mods` directory.

#### Server Side (Required):
1. Drop the corresponding `JamMod-<loader>-<version>.jar` into your server's `mods` directory.
2. *No extra ports need to be opened or forwarded; all synchronization runs through standard Minecraft custom payload packets.*

---

### Building from Source

Build targets individually or all at once using the included build manager:

```bash
# Build all supported loader versions:
./build_and_test.sh all

# Build a single version (e.g., 1.20.1 or 1.20.4):
./build_and_test.sh 1.20.1
./build_and_test.sh 1.20.4

# Launch test client:
./build_and_test.sh run 1.20.4
```