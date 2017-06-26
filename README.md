# Viro
A very simple, slightly modified version of the Minecraft PvP gamemode "Varo"

# Rules
Viro is a teamed Minecraft PvP gamemode. The teams you created fight each other and whenever a user gets killed, their team loses a live and the player gets temporarly banned for the time specified in the config. When the lives of a team reach 0, the team has lost. When only one team is left, that team wins.

If a user leaves the server before the minimum online-time (specified in the config), they get banned as well and their team also uses a live. The same thing happens when a user leaves the server while in a battle.

# Usage
To start the game, simply type `/start`. To stop it, type `/stop`. In theory, you can play right out of the box but you should create a least two teams first.

**Aliases for /start:** `/vs, /vstart`

# Teams
**Team chat:** To write a message that only the members of your team can see, set an '@' right at the beginning of your message.

## Creating teams
You can use `/teams create <name> [color]` to create a team. `<name>` can be any value that doesn't contain spaces and has a max length of 32 characters. You can use all of unicode in it. `[color]` can be used to set the color of the team. If no color is specified, the plugin will auto-choose one that isn't already occupied. The following colors can be set:
```
AQUA
BLUE
DARK_AQUA
DARK_BLUE
DARK_GREEN
DARK_PURPLE
DARK_RED
GOLD
GREEN
LIGHT_PURPLE
RED
YELLOW
```
If there are no colors left, you can't create any more teams. Thereby, you can only create a maximum of 12 teams.

## Deleting teams
To delete a team, you can use `/teams delete <name>`.

## Adding members to teams
If you want to add a player to a team, use `/teams add <teamName> <playerName>`.

## Removing members from teams
Similar, you can use `/teams remove <teamName> <playerName>` to remove a player from a team.

## List all teams
To view a list of all teams, use `/teams list`. To show the members of a team, use `/team list <teamName>`.

**Aliases:** `/vt, /vteams`

# Changing the settings
The `/configuration` command can be used to show or change different settings for the next round.
To show a setting, use `/configuration <settingName>`. To change a setting, use `/configuration <settingName> <value>`. To reset the settings to their default state, use `/configuration init`.

The following settings are availiable:
```
+---------------+---------+-------------------------------------------------------------------------+
| Name          | Type     | Description                                                            |
+---------------+----------+------------------------------------------------------------------------+
| lives         | Integer  | The lives each team has                                                |
| banDuration   | Double   | The amount of time a user is banned on their death in hours            |
| playDuration  | Double   | The minimum amount of time a user has to stay on the server in minutes |
| peaceDuration | Double   | The length of the peace period after the start in minutes              |
| worldborder   | Integer  | The side-length of the worldborder in blocks                           |
+---------------+----------+------------------------------------------------------------------------+
```

**Aliases:** `/vc, /vconf, /vconfig`

# Adding operators
An operator gets set to creative mode in the beginning and they are intended to watch over and operate the game.
To add an operator, use `/operators add <name>`. To remove an operator, use `/operators remove <name>`.

**Aliases:** `/vops`

# Adding spectators
A spectator gets set to spectator mode in the beginning and they only watch the game.
To add a spectator, use `/spectators add <name>`. To remove a spectator, use `/spectators remove <name>`.

**Aliases:** `/vspecs, /vsp`

# Other commands
To view the winner of the last round, use `/lastwinner`.

**Aliases:** `/vlast, /vwinner, /vlw, /vl`

To view basic informations about the plugin, use `/info`.

# Future features/TODO
- [ ] Clean up the code
- [ ] Add possibility to change the maximum time between two logins of one team
- [ ] Add the possibility to re-use colors
- [ ] Add autocomplete for everything
- [ ] Add feature to automatically shrink the worldborder after a given time
- [ ] Add meassurment units to config outputs
- [ ] What ever comes to my mind

# Known issues
 - When logging in while being banned the client sometimes throws a `java.io.IOException` instead of the message how long the user is still being banned.

# Permissions
```
viro.start          # User can start a round
viro.stop           # User can stop the current round
viro.config         # User can use the /configuration command
viro.operators      # User can add/remove viro operators
viro.spectators     # User can add/remove spectators
viro.teams          # User can manage teams
```
