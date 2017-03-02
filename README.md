# Momo, your new Discord bot
[![license](https://img.shields.io/github/license/paul-io/momo-2.svg)](https://github.com/paul-io/momo-2/blob/master/LICENSE) [![Dependencies](https://app.updateimpact.com/badge/809606116261629952/Momo%20Discord%20Bot.svg?config=test)](https://app.updateimpact.com/latest/809606116261629952/Momo%20Discord%20Bot) [![Dependency Status](https://www.versioneye.com/user/projects/58677499e78d7d00471b7787/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58677499e78d7d00471b7787) [![GitHub release](https://img.shields.io/github/release/paul-io/momo-2.svg)](https://github.com/paul-io/momo-2/releases) [![join the Momo discord server](https://discordapp.com/api/guilds/259125580744753153/embed.png)](https://discord.gg/uM3pyW8) 

Join the discord server linked above to get support, see upcoming features, or to test the bot! Also, check out the website for a command list & bot status [here](https://momobot.io)

**Momo** is a simple-to-use Discord bot based off of [JDA](https://github.com/DV8FromTheWorld/JDA).  From sending Twitch.tv notifications to playing music, and from pulling anime theme songs off of [Themes.moe](https://themes.moe) to temporarily muting troublemakers, Momo can do a lot for your server.


## Adding Momo to your server
Visit [this link](https://discordapp.com/oauth2/authorize?client_id=259137993351102464&scope=bot&permissions=268435518) and select which server you want it to join. The bot will give you a bit of an intro message letting you know everything is a-ok. It's as easy as that!
#### What commands are available?
Send the command `$help` or visit the website at [this link](https://momobot.io/commands.html). If you need more information on specific commands, you can try `$help command-name` or just PM the bot the command name.
###Web dashboard
Introduced in update 1.2, Momo features a [web dashboard](https://momobot.io/dash) to configure various aspects of the bot, such as welcome prefix, enabled commands, and Twitter/Reddit feeds. You don't have to be the server owner, either - configuration starts from moderators with the `kick` permission. Everything that can be done through the dashboard can also be done through commands.
### Features? Gimme some info!
* Reddit, Twitter, & Twitch.tv feeds - Get updates directly to your channel of choice with image/preview configuration
* Play music in a music channel. Can play off direct Youtube searches, too!
* Web dashboard. Configure your server settings from the ease of your browser from the [dashboard](https://momobot.io/dash)
* Role management: Set roles as *joinable* and allow users to join/leave at their whim
* Bring up character for various video games: FFXIV, WoW, osu! *(LoL & Overwatch coming soon!)*
* Commands to ban, kick, and prune messages
* Create a strawpoll from discord & directly link it to your users
* Log channel for user join/leaves, bans, kicks, and nickname changes
* And more!

---

## Hosting Momo for yourself
`you need java 8 to run this bot`

* If you want to host your own instance of the bot, feel free to take a look at the Releases tab and download the package. Fill out the configuration in the `resources/Bot.properties` file, then run `java -jar momo-x.x.x.jar` where `x.x.x` is the current version. 
* To obtain a bot token from Discord, head on over to the [Discord Developers](https://discordapp.com/developers/applications/me) page. From there, you can create an Application, then convert it to a Bot account. Then, click to show the bot token, which you can copy and paste into `Bot.properties`
* Hosting Momo for yourself nets you some benefits. Music functionality, though dependent on your internet speeds, will be better for single servers than a larger cluster. You can also change its username, avatar, and game status to whatever you see fit!

---

## Pulling from the source & building
Momo uses [Apache Maven](https://maven.apache.org/) for project management. As such, it's extremely simple managing Java dependencies, so building any edits and changes you want into your own bot is easy.

#### Installing Maven
Linux: `apt-get install maven`

Windows & macOS: [download the package](http://maven.apache.org/download.cgi) and follow the instructions in the previously linked install page

**Windows & macOS alternative**

Windows & macOS users can install [Chocolatey](https://chocolatey.org/) & [Homebrew](http://brew.sh/) respectively to get `apt-get` functionality

chocolatey: `choco install maven`

homebrew: `brew install maven`

#### Building
Run `mvn install` on the root directory. This will create two builds: a `.jar` of the bot's source & a `.jar` with all the dependencies shaded (all packaged into a single file). This is the file you want - `momo-x.x.x.jar`. On subsequent builds, if you do not run the command with the `clean` parameter, then all `.jar` will be the correct bot.

**NOTE**: `mvn install` *does not* copy the resources folder to the `target/` directory. As a side effect, it *will not* overwrite pre-existing resources, so you are free to copy over `resources/` to `target/`.

**NOTE 2**: If you decide to run `mvn clean install`, *all folders and files in* `target/` *will be deleted*. Just a forewarning before you lose all of your server's data

#### Running
Once you have built the jar, simply run `java -jar momo-x.x.x.jar` where `x.x.x` is the current version numbering. 

### Creating a command
Probably the #1 reason people will run their own bot, and probably the easiest thing to implement with Momo. This example also shows how permissions are setup, so if you want to change the permission level of commands... You're in the right place.

1. Create a new class file. Must be in the `io.ph.bot.commands` package.

2. Let's say you call your command `Echo`, and it echoes whatever the user says. Make sure to have your file extend `io.ph.bot.commands.Command` and to override `run(Message msg)`

3. The meat of your command goes in the aformentioned `run` method. For brevity, our command ignore package and imports.
```java
public class Say extends Command {
    @Override
    public void run(Message msg) {
        msg.getChannel().sendMessage(Util.getCommandContents(msg)).queue();
    }
}
```
To then have the command register through the command handler, annotate the class with `io.ph.commands.CommandData`
```java
@CommandData (
		defaultSyntax = "echo",
		aliases = {"repeat", "ech0"},
		permission = Permission.NONE,
		description = "Have the bot repeat after you",
		example = "This will be echoed!"
		)
public class Say extends Command {
    @Override
    public void run(Message msg) {
		msg.getChannel().sendMessage(Util.getCommandContents(msg)).queue();
    }
}
```
It's as easy as that~ 

note: commands with permission `Permission.NONE` are disableable by admins by using the `disable` command

If you're going to delve deeper into developing with JDA, check out the documentation [here](http://home.dv8tion.net:8080/job/JDA/Promoted%20Build/javadoc/) and join up at the [Discord API server](https://discordapp.com/invite/0SBTUU1wZTWPnGdJ).