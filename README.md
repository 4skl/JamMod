# JamMod

JamMod brings synchronized multiplayer music and audio streaming directly into Minecraft.

---

### Features

- **Zero-Drift Multiplayer Sync:** Server-authoritative timestamps keep track positions synchronized down to the millisecond across all listeners. Pausing, resuming, and late-joining automatically align playback without audio drift.
- **Unified Synced Jams:** Every room functions as a live, persistent playlist. Create a room, add media, and anyone in the session hears the same stream simultaneously.
- **Public & Private Rooms:** Host Public Jams that appear in the server directory or Private Jams restricted to invited players.
- **Granular Roles & Permissions:**
  - **Host & Admin:** Manage room privacy, cycle edit policies, assign roles, and remove members.
  - **Editor:** Add, remove, and reorder songs when the room is set to Collaborator mode.
  - **Listener:** Stream audio in sync with the room.
  - **Edit Policies:** Configurable between *Owner Only*, *Collaborators*, and *Open to All*.
- **Timeline Seekbar & Scrubbing:** Click directly on the timeline bar in the GUI or use `/jam seek <seconds>` to jump to any timestamp across all connected listeners.
- **Queue Modes:** Support for Loop All (`🔁`), Loop Single Track (`🔂`), and Queue Shuffle (`🔀`).
- **Track Reordering & Live Metadata:** Move tracks up or down (`▲` / `▼`) in the queue. Track titles and durations resolve automatically in the background via Lavaplayer.
- **In-Game HUD Mini-Player:** An unobtrusive, animated overlay displays the song title and icon in the top-right corner whenever a track changes. The display timer pauses while screens are open so you never miss an alert.
- **Native Music Volume Binding:** Volume controls link directly to Minecraft's vanilla `Music` sound slider via an in-GUI slider or the `/jam volume` command.
- **Clickable Chat Links & Anti-Spam:** Room invitations and `/jam list` entries feature clickable text to join rooms with one click. Built-in rate limits prevent spam, and players can opt out of invitations with `/jam toggleinvites`.
- **Bandwidth Saver:** When all players leave a Jam, playback automatically idles to conserve server and network resources.
- **Standalone Architecture:** Completely self-contained with no companion mods, external servers, or native binary installations required.

---

### Requirements & Installation

#### Client Side (Required):
- Minecraft Forge (1.20.1 - 47.4.28+)
- JamMod jar in your `.minecraft/mods` directory

#### Server Side (Required):
- Minecraft Forge (1.20.1 - 47.4.28+)
- JamMod jar in your server's `mods` directory
- *No extra ports need to be forwarded; all synchronization runs through standard Minecraft packets.*

---

### Keybindings & Controls

- **`J` (Default):** Open the Jam Session & Directory GUI.

---

### Commands Guide (`/jam`)

All operations can be managed via the graphical interface or through chat commands. Required parameters are in `< >`, and optional parameters are in `[ ]`.

#### Room & Directory Management
| Command | Description |
| :--- | :--- |
| `/jam create <name> [true\|false]` | Create and join a new Jam. Set to `true` for Private. |
| `/jam join <name\|id>` | Join an active Jam and sync playback. |
| `/jam leave` | Disconnect from your current Jam session. |
| `/jam list` | List all accessible Public and Private Jams (with clickable join links). |
| `/jam delete <name>` | Delete a Jam room you own. |
| `/jam status` | Display the active Jam's tracklist, host, role, and playback state. |

#### Playback & Queue Controls
| Command | Description |
| :--- | :--- |
| `/jam play [url]` | Play a URL immediately, or resume playback if paused. |
| `/jam add <url>` | Append a media URL to the queue. |
| `/jam pause` | Pause playback for all listeners in the room. |
| `/jam resume` | Resume playback for all listeners in the room. |
| `/jam toggle` | Toggle between play and pause. |
| `/jam skip` | Skip to the next track in the queue. |
| `/jam prev` | Jump back to the previous track. |
| `/jam jump <index>` | Jump directly to track number `#` in the queue. |
| `/jam move <from> <to>` | Reorder track from one queue position to another. |
| `/jam remove <index>` | Remove track `#` from the playlist. |
| `/jam seek <seconds>` | Seek playback to a specific second mark in the active song. |
| `/jam repeat` | Cycle queue repeat mode: *Off*, *All*, or *One*. |
| `/jam shuffle` | Toggle queue shuffle mode on or off. |

#### Volume Control
| Command | Description |
| :--- | :--- |
| `/jam volume <0-100>` | Adjust local Minecraft Music volume directly (alias: `/jam vol`). |

#### Roles, Permissions & Invites
| Command | Description |
| :--- | :--- |
| `/jam policy` | Cycle edit permissions (*Owner Only* / *Collaborators* / *Open to All*). |
| `/jam privacy` | Toggle room visibility between Public and Private. |
| `/jam role <player> <admin\|editor\|listener>` | Set a member's role and permissions in the Jam. |
| `/jam invite <player>` | Send a clickable Jam invite to an online player. |
| `/jam toggleinvites` | Toggle blocking/allowing incoming Jam invitations. |
| `/jam help` | Display an interactive, clickable command guide. |