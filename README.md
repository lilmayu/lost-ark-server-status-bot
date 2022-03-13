# Lost Ark Server Status Bot
Made by `mayuna#8016`

## Disclaimer
As you can see, this bot is now open-source. However, the source code is not meant
to be readable for everyone. If you have any questions regarding to the source code,
you can contact me via my [Discord support server](https://discord.gg/YMs6wXPqcB).

**Please respect project's license (GNU GPLv3)**, also you should credit this repostitory in your derived works

## Self-hosting
1. Download sources and compile it (with gradle task `shadowJar`)
    - You can also download the pre-compiled jar via [Releases section](https://github.com/lilmayu/lost-ark-server-status-bot/releases)
      or via [Github's actions](https://github.com/lilmayu/lost-ark-server-status-bot/actions) (those are rather development versions
      and can have bugs in them, [Releases section](https://github.com/lilmayu/lost-ark-server-status-bot/releases) should be
      tested and working right)
2. Run the compiled/downloaded jar with command `java -jar <file>`, you can also specify how much memory it will have via `-Xmx*G` 
   (or `-Xmx*M` for value in MBs) argument for example: `java -Xmx1G -jar lost-ark-server-status-bot.jar`
    - Note: The minimum memory it can run with is something around 256 MB
3. Bot's config should be generated next to the jar (or path, where it ran)
    - You can leave prefix empty
    - You can get token from [Discord's Developer Portal](https://discord.com/developers/applications)
    - `exceptionMessageChannelID` can be empty, however you can specify it with Text Channel's ID and bot will be sending
      some exceptions there (usually if some command results in exceptions)
    - `ownerID` is your Discord ID
    - `debug` does nothing, I think
    - `westNorthAmerica`, `eastNorthAmerica`, `centralEurope`, `southAmerica`, `europeWest` are string arrays with server names per region,
      you can get filled copy [here](https://pastebin.com/raw/KA1ZKUjz)
    - `contributors` is also a string array, which is visible in `/about` command
    - `logLevel` specifies which log level is used when logging (for example `info`, `error`, `debug`, ...)
4. After filling bot's config, you can rerun the jar with command in step 2.
5. You can create bot's invite link by clicking your created Apllication on [Discord's Developer Portal](https://discord.com/developers/applications)
    -> OAuth2 -> URL Generator -> Scopes: "bot", "applications.commands" -> Bot permissions: "Send Messages" (or Administrator, its your choice) -> Invite URL is in Generated URL box
6. Add the bot on your server!
### NOTE
You **should** credit this repository in your bot's About Me section (via a URL): Your application on [Discord's Developer Portal](https://discord.com/developers/applications) -> General Information -> Description

**If your self-hosted bot would reach a 100 servers, you may not verify it since it is not your work**, however the choice is your.
