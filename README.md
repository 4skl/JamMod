# JamMod

[![Minecraft Forge](https://img.shields.io/badge/Minecraft%20Forge-1.18.2%20|%201.19.2%20|%201.19.4%20|%201.20.1-orange.svg)](https://files.minecraftforge.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-1.20.4%20|%201.21.1-red.svg)](https://neoforged.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**JamMod** brings synchronized, zero-drift multiplayer audio and music streaming directly into Minecraft. Listen to synchronized YouTube, SoundCloud, direct audio streams, and radio with friends—without external Discord bots or third-party desktop apps.

---

### Supported Minecraft & Loader Matrix

| Minecraft Version | Mod Loader | Minimum Loader Version | Stonecutter Target |
| :--- | :--- | :--- | :--- |
| **1.18.2** | Forge | 40.2.0+ | `1.18.2-forge` |
| **1.19.2** | Forge | 43.3.0+ | `1.19.2-forge` |
| **1.19.4** | Forge | 45.2.0+ | `1.19.4-forge` |
| **1.20.1** | Forge | 47.1.0+ | `1.20.1-forge` |
| **1.20.4** | NeoForge | 20.4.190+ | `1.20.4-neoforge` |
| **1.21.1** | NeoForge | 21.1.65+ | `1.21.1-neoforge` |

---

### Getting Started

#### 1. Open the Menu
* Press **`J`** in-game to toggle the JamMod interface.
* If no rooms exist on the server, an onboarding guide will walk you through launching your first session.

#### 2. Create a Jam Session
* Enter a room name and click **Create Jam**, or run:

```

/jam create  [private]

```
* **Public Jams** appear in the server directory for all players to discover and join.
* **Private Jams** are hidden and require a direct invite from the Host or an Admin.

#### 3. Add Music & Audio
* Click **`[+ Add Track]`** and paste any stream URL.
* Supported sources include:
- **YouTube** (videos, live streams, and playlists)
- **SoundCloud** tracks and playlists
- **Bandcamp**, **Twitch Streams**, and direct web audio files (`.mp3`, `.ogg`, `.flac`, `.wav`).
* Titles, artists, and song durations resolve automatically in the background.

#### 4. Invite & Listen in Sync
* Click **`[Invite]`** or use `/jam invite <player>`. Invited players receive a clickable chat invitation link.
* Audio syncs down to the millisecond across every listener on the server.

---

### Role & Permission Hierarchy

| Role | Permissions |
| :--- | :--- |
| **Host (Owner)** | Total room authority: toggle privacy, delete room, modify policies, transfer ownership, manage all roles. |
| **Admin** | Room moderation: cycle room edit policies, invite players, promote/demote Editors and Listeners, remove members. |
| **Editor** | Queue control: add songs, delete tracks, reorder queue (when allowed by room policy). |
| **Listener** | Audio stream: listen along, vote skip, adjust personal local volume. |

#### Edit Policies
* **Host Only (`HOST_ONLY`):** Only the room owner and admins can add or reorder tracks.
* **Collaborators (`COLLABORATORS`):** Hosts, Admins, and designated Editors can queue tracks.
* **Open to All (`OPEN`):** Any listener can add tracks to the queue.

---

### In-Game Command Reference

| Command | Description |
| :--- | :--- |
| `/jam` | Opens the graphical JamMod interface (or press **`J`**). |
| `/jam list` | Displays all visible active jams on the server. |
| `/jam create <name> [private]` | Creates a new Jam session (optionally private). |
| `/jam join <name\|id>` | Joins an existing Jam room. |
| `/jam leave` | Leaves your current Jam room. |
| `/jam invite <player>` | Invites an online player to your session. |
| `/jam add <url> [now]` | Adds a track to the queue (pass `true` to play immediately). |
| `/jam play` / `/jam pause` | Resumes or pauses playback for everyone in the room. |
| `/jam skip` | Skips to the next track in the queue. |
| `/jam prev` | Restarts the current track or returns to the previous one. |
| `/jam seek <seconds>` | Jumps playback to an exact timestamp for all listeners. |
| `/jam volume <0-100>` | Adjusts your local JamMod volume (synced with Minecraft Music). |
| `/jam hud [toggle\|corner]` | Configures the "Now Playing" HUD overlay on your screen. |
| `/jam toggleinvites` | Toggles whether other players can send you room invitations. |

---

### Client HUD Mini-Player

When a new track begins, an unobtrusive "Now Playing" notification appears in your chosen corner:
* Displays the resolved track title along with the source URL subtext.
* Fully customizable: Choose between `TOP_RIGHT`, `TOP_LEFT`, `BOTTOM_RIGHT`, or `BOTTOM_LEFT`.
* The popup timer automatically freezes while menus or inventories are open so track notifications aren't missed.
* Preferences persist locally in `.minecraft/config/jammod_client.json`.

---

### Installation

#### Client-Side
1. Install **Forge** (1.18.2 – 1.20.1) or **NeoForge** (1.20.4 – 1.21.1).
2. Drop the matching `JamMod-<version>.jar` into your `.minecraft/mods` directory.

#### Server-Side
1. Place the matching `JamMod-<version>.jar` into your server's `mods` directory.
2. **No extra ports need to be opened or forwarded**; all communication uses Minecraft custom network payloads.

---

### Building from Source

This project uses **Stonecutter** for multi-version management.

```bash
# Clone the repository
git clone [https://github.com/mediolivier/JamMod.git](https://github.com/mediolivier/JamMod.git)
cd JamMod

# Switch active Stonecutter project to any version:
./gradlew "Set active project to 1.20.4-neoforge"
./gradlew "Set active project to 1.21.1-neoforge"
./gradlew "Set active project to 1.20.1-forge"

# Compile and build the active version JAR:
./gradlew build

# Run the test client for the active target:
./gradlew runClient

```

### License

Distributed under the **MIT License**. See `LICENSE` for details.