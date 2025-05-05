# Server Configuration

RW-HPS provides **3** configuration files:
- [Config.json](#configjson) for general settings
- [ServerConfig.json](#serverconfigjson) for Server mode settings
- [ConfigRelay.json](#configrelayjson) for Relay mode settings

**Note:** RW-HPS does **not support** hot reloading of configurations. Please stop the server before making any changes to the configuration files!

## Config.json

### defStartCommand
#### Default server start command (str)
- **Server mode:** `start`
- **Relay mode:** `startrelay`/`startrelaytest`
  > `startrelay` directly forwards and consumes more bandwidth, similar to using a VPN.
  > `startrelaytest` uses multicast, reducing bandwidth consumption.

Default: `start`

### log
#### Log level (str)
Sets the logging level; the higher the level, the more detailed the output.
> Incorrect input defaults to `ALL`.
> Options: `OFF`, `FATAL`, `ERROR`, `WARN`, `INFO`, `DEBUG`, `TRACE`, `ALL`

Default: `WARN`

### cmdTitle
#### Terminal title (str)
RW-HPS changes the terminal title upon startup:
- On Windows, it uses native methods.
- On Linux, it uses vt100 control characters.

If not configured, it displays:
> `[RW-HPS] Port: 5123, Run Server: ${NetStaticData.ServerNetType.name}`

Default: Not configured

### followBetaVersion
#### Use beta versions for updates (bool)
RW-HPS allows updating the server from the console by entering `tryupdate`.
- If enabled, it uses beta versions for updates, allowing you to experience new features (and potentially new bugs) in advance.
- If disabled, it uses stable versions for updates.

Default: Disabled

### port
#### Server port (int)
Specifies the port used by the server.

Default: `5123`

### serverName
#### Server name (str)
The name of the server as shown in the list.

Default: `RW-HPS`

### subtitle
#### Map name (str)
The map name displayed in the server list. If not configured, it defaults to the map name.

Default: Not configured

### autoUpList
#### Auto upload list (bool)
Automatically adds the server to the list on startup (equivalent to `uplist add`).

Default: Disabled

### ipCheckMultiLanguageSupport
#### Multi-language support based on IP (bool)
Determines the player's country based on their IP and displays the language accordingly (provided a translation file is available).

Default: Disabled

### singleUserRelay
#### Single-user Relay mode (bool)
Similar to Relay mode but with only one room.

Default: Disabled

### singleUserRelayMod
#### Mod support for single-user Relay mode (bool)
Enables mod support for single-user Relay mode.

Default: Disabled

### webToken
#### HTTP API token (str)
Token used for [HttpApi](../api/HttpAPI.md).

Default: Randomly generated

### webHOST
#### HTTP host restriction (str)
Restricts HTTP access to the specified host, preventing IP access.

Default: Not configured

### webPort
#### HTTP server port (int)
Specifies the port used by the HTTP server; `0` disables the HTTP server.
**Note:** Port reuse is **not supported**; do not set it to the same port as the game server.

Default: Disabled

### ssl
#### Enable HTTPS (bool)
**Not recommended** to use SSL.
Enables SSL for the HTTP server (i.e., HTTPS).

Default: Disabled

### sslPasswd
#### SSL certificate password (str)
**Warning:** Plain text password.
To configure SSL, place `ssl.jks` (case-sensitive) in the same directory as the jar file and enter the JKS certificate password here.

Default: Not configured

### runPid
#### Server run PID (int)
You don't need to know or modify this setting.
> If you are curious:  
> It provides the current JVM's PID, facilitating the use of other programs to shut down the JVM.

## ServerConfig.json

### enterAd
#### Enter message (str)
Message sent to players when they enter the server.

Default: Not configured

### startAd
#### Start message (str)
Message sent to players when the game starts.

Default: Not configured

### maxPlayerAd
#### Full server message (str)
Message sent to players when they try to join a full server.

Default: Not configured

### startPlayerAd
#### Game in progress message (str)
Message sent to players when they try to join a server where the game is already in progress.

Default: Not configured

### passwd
#### Server password (str)
Sets a password for the server; players need to enter the password to join. If not configured, no password is required.

Default: Not configured

### maxPlayer
#### Maximum players (int)
Sets the maximum number of players the server can accommodate.

Default: `10`

### maxGameIngTime
#### Maximum game time (int)
Sets the maximum game duration. When this time is reached, the room is closed (but not the server).
- Setting to `-1` means unlimited.
- Unit: seconds (sec)

Default: `7200`

### maxOnlyAIGameIngTime
#### Maximum game time without other players (int)
When only AI players are present (i.e., only a group of AIs), the room is closed (but not the server) when this time is reached.
- Setting to `-1` means unlimited.
- Unit: seconds (sec)

Default: `3600`

### startMinPlayerSize
#### Minimum players to start (int)
The minimum number of players required to start the game.
- Setting to `-1` means no restriction.

Default: No restriction

### autoStartMinPlayerSize
#### Automatic start player count (int)
When the number of players is greater than or equal to this value, the server automatically starts the game.
- Setting to `-1` disables this feature.

Default: `4`

### maxMessageLen
#### Maximum message length (int)
The maximum length of messages allowed on the server.

Default: `40`

### maxUnit
#### Maximum units (int)
The maximum number of units allowed on the server.

Default: `200`

### defIncome
#### Default income multiplier (float)
The default income multiplier for the server (supports decimals).

Default: `1.0`

### turnStoneIntoGold
#### Turn stone into gold (bool)
Buildings and units require **no** time and **no** funds.

Default: Disabled

### oneAdmin
#### Make the first player an admin (bool)
The server makes the first player to join an admin.

Default: Enabled

### saveRePlayFile
#### Save replay files (bool)
The server allows clients to save replay files for reviewing the game process.

Default: Enabled

## ConfigRelay.json

### mainID
#### Prefix ID (str)
Prefix ID for rooms in Relay mode. If not configured, defaults to numeric only.

Default: Not configured

### mainServer
#### Is the RELAY main node (bool)
> The main node can assign sub-nodes with sub-IDs. For example, if RCN assigns RA, setting this to true means RA will be redirected; if false, it will be parsed.

Default: Enabled

### upList
#### TODO (bool)
Whether the server supports uplist.

Default: Disabled

### mainServerIP
#### TODO (str)

### mainServerPort
#### TODO (int)

> Wondering why it's `TODO`?  
> Actually, I don't know either.  
> ~~Maybe Dr. was just lazy.~~