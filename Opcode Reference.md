This list of opcodes is for the client (each shard of the companion bot). The client always initiates the connection handshake.

## OUTGOING TO MAIN BOT
##### 1 - Initial handshake
This is sent on bot initialization, and will retry if the connection is closed.
```
{
	op: 1,
	key: String,
	guildIds: [ long, long, ...]
}
```

##### 2 - Request for guild information
This is sent when a guild invites the client, and requests an opcode 2

```
{
    op: 2,
    key: String,
    guildId: long
}
```

##### 5 - Send current music stats
This is sent periodically (every 30 seconds) and contains data for all shards on the client and how many are playing music

```
{
    op: 5,
    key: String,
    shardId: int,
    playingMusic: int
}
```


## INCOMING FROM MAIN BOT
##### 1 - Initial handshake response
This only responds with guild information for the ones sent out. Don't worry about supporterIds
```
{
	op: 1,
	supporterIds: [ long, long, ...]
	guilds: [ 
				{
				guildId: long,
				prefix: String,
				djRoleId: long, (can be 0 to indicate no set DJ role)
				voiceChannelId: long, (can be 0 to indicate no set channel)
				musicChannelId: long, (can be 0 to indicate no set channel)
				}, ...
			]
}
```

##### 2 - Incoming guild information
This is broadcast to all clients when a new server is initialized from the server or is requested. It is up to the client to add it to its guild map if it is actually connected to that shard
```
{
    op: 2,
    guildId: long,
	prefix: String,
	djRoleId: long, (can be 0 to indicate no set DJ role)
	voiceChannelId: long, (can be 0 to indicate no set channel)
	musicChannelId: long, (can be 0 to indicate no set channel)
}
```

##### 3 - Guild change in command status
Guild decided to enable or disable the music command, send to all clients
```
{
    op: 3,
    enable: boolean
}
```

##### 10 - Add new supporter
This can be ignored if you are hosting it yourself - supporters (patreon) is only on the main bot
```
{
	op: 10,
	supporterId: long
}
```

##### 11 - Remove supporter
This can be ignored if you are hosting it yourself - supporters (patreon) is only on the main bot
```
{
	op: 11,
	supporterId: long
}
```