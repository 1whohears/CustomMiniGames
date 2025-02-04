# Dependencies

This mod requires [1wholibs!](https://www.curseforge.com/minecraft/mc-mods/onewholibs)

# Summary

This is a minecraft forge mod that adds some customizable mini-games that can be started using commands. The mod currently includes free for all/team death-match, simple buy-attack phase game mode, and a kill the flag game mode. 

The games were designed to work in SMPs. For example, players can make their teams and put on the best gear that they have, and start a team death-match mini-game. The mod will automatically put the players that ran out of lives into spectator mode. 

The mod also includes a custom kit and shop system. Server devs can add custom kits and shops via datapacks. 

The mod is intended to allow other mod devs to create their own custom minigames using this mod's frame work. Complete documentation on how to do this will come when this mod is stable. 

This mod is currently in beta and has many bugs. I recommend immediate respawn to be turned on!

# How to Use

Here is a basic map of a mini-game's life cycle:

1) Create a new mini-game instance with `/minigame create_new <game_type> <instance_id>`

2) Add players/teams to this game instance `/minigame setup <instance_id> add_player/add_team <name>`

3) Configure the game instance with the setup command. One can see all the different options with the command suggestions `/minigame setup <instance_id> <parameter> <value>` These settings are saved in the world. 

4) Start the mini-game `/minigame setup <instance_id> start` If the game doesn't start, read the error. There may be a parameter that you didn't configure. 

5) You can reset the game at anytime with `/minigame reset <instance_id> confirm_reset` Note that reset doesn't undo the settings in steps 2 and 3. It is mostly used to play the same game configuration again.

6) If you want to remove that game configuration from the world `/minigame remove <instance_id> confirm_remove`

More details about all the different `setup` settings will come out later. But most of them are fairly self explanatory. 

Players that don't have op can use `/kit` and `/shop` to select a kit and open a shop.
