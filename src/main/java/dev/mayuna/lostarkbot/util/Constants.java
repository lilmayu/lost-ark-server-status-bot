package dev.mayuna.lostarkbot.util;

import dev.mayuna.lostarkbot.objects.other.TwitterUser;

public class Constants {

    public static final String CONFIG_PATH = "./bot_config.json";
    public static final String POSTS_HASHES_JSON = "./posts_hashes.json";

    public static final String GUILDS_FOLDER = "./guilds/";
    public static final String LANG_FOLDER = "./lang/";
    public static final String STEAM_API_URL = "https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?format=json&appid=1599340";

    public static final String VERSION = "2.1beta_08052022a";

    public static final String ONLINE_EMOTE = "<:Online:968167180749381692>";
    public static final String BUSY_EMOTE = "<:Busy:968167180992651294>";
    public static final String FULL_EMOTE = "<:Full:968167180657098763>";
    public static final String MAINTENANCE_EMOTE = "<:Maintenance:968167180841680936>";
    public static final String OFFLINE_EMOTE = "<:Offline:968167180493537382>";

    public static final String TWITTER_LOGO_URL = "https://i.imgur.com/QvQPU4a.png";

    public static final TwitterUser[] TWITTER_USERS = new TwitterUser[]{
            new TwitterUser("playlostark", 1291477342624210944L),
            new TwitterUser("PlayLostArkES", 1308465894461304832L),
            new TwitterUser("PlayLostArkFR", 1291507884161785856L),
            new TwitterUser("PlayLostArkDE", 1291503900915195904L)
    };

    public static final TwitterUser[] TWITTER_USERS_TEST = new TwitterUser[]{
            new TwitterUser("asdkhsabdjhqa", 1067457730439462912L)
    };
}
