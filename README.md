# Not Alone

A Minecraft Mod that adds a subtle Herobrine to the game.

The mod is designed to be installed and forgotten about, as it's intent is to make the player question if what they saw was actually there or if they imagined it.

Features so far:
- Herobrine (can appear and stalk the player, disappears when in Player's FOV)
- Animal possession (Herobrine can possess animals around you, they will get white eyes and stalk you, until looked at)
- Footsteps (Footsteps can play at random times behind the player)

## Configuration

Every feature can be turned off on its own, and how often it happens can be tuned,
in `config/notalone.toml`. The file is created with its default values the first
time the mod runs, and each option is commented in place. Delete it to start over.

```toml
[herobrine]
enabled = true
rarity = 2000

[footsteps]
enabled = true
rarity = 4000

[animal_possession]
enabled = true
rarity = 2000
```

`rarity` is a one-in-N chance rolled once per server tick, and there are 20 ticks
in a second, so a **higher** number means the event happens **less** often.

Every feature runs on the server side. In singleplayer that is your own game, so
the file in your instance folder is the one that matters. On a multiplayer server
only the **server's** copy has any effect: changing it on a client does nothing,
though the mod still has to be installed on both.

## Multiplayer

The mod also works in multiplayer and needs to be installed on both the server and client.

The mod will choose a player at random every 20 minutes, during this period it's possible for Herobrine to appear in relation to the player, while being mindful of other players as well.

This mod is particularly good if added to a modpack given to a friend or group of friends without them knowing, allowing for a potentially real and genuine unsettling experience that they will not be able to explain, since the mod is subtle and not explicitly horror.

