# JamMod

JamMod brings synchronized multiplayer music and audio streaming directly into Minecraft[cite: 20, 21].

---

### Features

- **Zero-Drift Multiplayer Sync:** Server-authoritative timestamps keep track positions aligned for all listeners down to the millisecond[cite: 20, 21]. Pausing, resuming, and late-joining automatically align playback without audio drift[cite: 20, 21].
- **Persistent Synced Jams:** Every room acts as a live, persistent playlist[cite: 21]. Create a room, add media, and anyone in the session hears the same stream simultaneously[cite: 21].
- **Public & Private Rooms:** Host Public Jams that appear in the server directory, or Private Jams restricted to whitelisted members[cite: 21].
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
- **Bandwidth Saver:** When all players leave a Jam, playback automatically idles to conserve server and network resources[cite: 21].
- **Standalone Architecture:** Completely self-contained with no companion mods, external servers, or native binary installations required[cite: 20, 21].

---

### Requirements & Installation

#### Client Side (Required)[cite: 20, 21]:
- Minecraft Forge (1.20.1 - 47.4.28+)[cite: 20, 21]
- JamMod jar in your `.minecraft/mods` directory[cite: 20, 21]

#### Server Side (Required)[cite: 20, 21]:
- Minecraft Forge (1.20.1 - 47.4.28+)[cite: 20, 21]
- JamMod jar in your server's `mods` directory[cite: 20, 21]
- *No extra ports need to be forwarded; all synchronization runs through standard Minecraft packets[cite: 20, 21].*

---

### Keybindings & Controls

- **`J` (Default):** Open the Jam Session & Directory GUI.

---

### Commands Guide (`/jam`)

All operations can be managed via the graphical interface or through chat commands. Required parameters are in `< >`, and optional parameters are in `[ ]`.

#### Room & Directory Management
| Command | Description |
| :--- | :--- |
| `/jam create <name> [true\|false]` | Create and join a new Jam. Set to `true` for Private[cite: 21]. |
| `/jam join <name\|id>` | Join an active Jam and sync playback[cite: 21]. |
| `/jam leave` | Disconnect from your current Jam session[cite: 21]. |
| `/jam list` | List all accessible Public and Private Jams (with clickable join links)[cite: 21]. |
| `/jam delete <name>` | Delete a Jam room you own[cite: 21]. |
| `/jam status` | Display the active Jam's tracklist, host, role, and playback state[cite: 21]. |

#### Playback & Queue Controls
| Command | Description |
| :--- | :--- |
| `/jam play [url]` | Play a URL immediately, or resume playback if paused[cite: 21]. |
| `/jam add <url>` | Append a media URL to the queue[cite: 21]. |
| `/jam pause` | Pause playback for all listeners in the room[cite: 21]. |
| `/jam resume` | Resume playback for all listeners in the room[cite: 21]. |
| `/jam toggle` | Toggle between play and pause. |
| `/jam skip` | Skip to the next track in the queue[cite: 21]. |
| `/jam prev` | Jump back to the previous track[cite: 21]. |
| `/jam jump <index>` | Jump directly to track number `#` in the queue[cite: 21]. |
| `/jam move <from> <to>` | Reorder a track from one queue position to another. |
| `/jam remove <index>` | Remove track `#` from the playlist[cite: 21]. |
| `/jam seek <seconds>` | Seek playback to a specific timestamp in the active track. |
| `/jam repeat` | Cycle queue repeat mode: *Off*, *All*, or *One*. |
| `/jam shuffle` | Toggle queue shuffle mode on or off. |

#### Volume & HUD Settings
| Command | Description |
| :--- | :--- |
| `/jam volume <0-100>` | Adjust local Minecraft Music volume directly (alias: `/jam vol`). |
| `/jam overlay toggle` | Toggle the in-game HUD song overlay on or off. |
| `/jam overlay corner <corner>` | Set overlay position (`top_right`, `top_left`, `bottom_right`, `bottom_left`). |

#### Roles, Permissions & Invites
| Command | Description |
| :--- | :--- |
| `/jam policy` | Cycle edit permissions (*Owner Only* / *Collaborators* / *Open to All*). |
| `/jam privacy` | Toggle room visibility between Public and Private. |
| `/jam role <player> <admin\|editor\|listener>` | Assign a role to a member (online or offline). |
| `/jam invite <player>` | Send a clickable Jam invite to an online player[cite: 21]. |
| `/jam toggleinvites` | Toggle blocking/allowing incoming Jam invitations. |
| `/jam help` | Display an interactive, clickable command guide. |