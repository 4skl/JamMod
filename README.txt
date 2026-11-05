JamMod brings synchronized multiplayer music and audio streaming directly into Minecraft.



### Features:

- **Zero-Drift Multiplayer Sync:** Server-authoritative timestamps keep track positions aligned for all listeners. Pausing, resuming, and late-joining accurately tracks elapsed time down to the millisecond.

- **Direct Audio Streaming:** Streams audio dynamically from external links (YouTube, SoundCloud, direct audio URLs) without bloated resource packs.

- **Unified Synced Jams:** Say goodbye to temporary sessions! Every room is now a persistent, live playlist. Create a Jam, add tracks to it, and anyone who joins will hear exactly what you hear.

- **Public & Private Rooms:** Host Public Jams that anyone on the server can browse and join, or Private Jams where you control the guest list using an invite system.

- **Bandwidth Saver:** When all players leave a Jam, playback automatically idles to save server and client resources until someone joins back.

- **Full GUI & Command Parity:** Every feature available in the interactive GUI (default key: `J`) is fully mirrored by a robust `/jam` command system.

- **Standalone Architecture:** 100% self-contained. No external companion mods, native binary installations, or third-party authentication required.



### Requirements & Installation:



**Client Side (Required):**

- Minecraft Forge (1.20.1 - 47.4.28+)

- JamMod

*Note: JamMod is completely standalone and bundles its own audio engine. Just drop it into your mods folder!*



**Server Side (Required):**

- Minecraft Forge (1.20.1 - 47.4.28+)

- JamMod

*Note: The server only orchestrates room states, queues, and playback timestamps. It handles this entirely through standard Minecraft network packets (no extra ports need to be forwarded).*



---



### Commands Recap (`/jam`)



All actions can be performed either in the GUI or via these commands. Arguments in `< >` are required, and `[ ]` are optional.



#### Room & Directory Management

* `/jam create <name> [true|false]` - Create and join a new Jam. Set to `true` for a Private Jam.

* `/jam join <name|id>` - Join an existing Jam to sync up your audio.

* `/jam leave` - Leave your current Jam.

* `/jam list` - Browse all Public Jams and your Private Jams.

* `/jam delete <name>` - Delete a Jam you own.

* `/jam invite <player>` - Allow another player to join your Private Jam.

* `/jam status` - View your active Jam's current track, playback state, and listener count.



#### Playback & Playlist Controls

* `/jam add <url>` - Append a track to the end of the current Jam's playlist.

* `/jam play [url]` - Play an audio URL immediately, or resume playback if paused.

* `/jam pause` | `/jam resume` - Control the playback state of the room.

* `/jam skip` | `/jam prev` - Go to the next or previous track in the playlist.

* `/jam jump <index>` - Jump immediately to a specific song number in the playlist.

* `/jam remove <index>` - Remove a specific song number from the playlist.