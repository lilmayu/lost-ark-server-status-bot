package dev.mayuna.lostarkbot.util;

public class Constants {

    public static final String CONFIG_PATH = "./bot_config.json";
    public static final String GUILDS_FOLDER = "./guilds/";
    public static final String POSTS_HASHES_JSON = "./posts_hashes.json";
    public static final String STEAM_API_URL = "https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?format=json&appid=1599340";

    public static final String ALTERNATIVE_PREFIX = "lostark!";
    public static final String VERSION = "11042022a";

    public static final String ONLINE_EMOTE = "<:LA_online:954727071001755668>";
    public static final String BUSY_EMOTE = "<:LA_busy:954730090862882826>";
    public static final String FULL_EMOTE = "<:LA_full:954728445684879400>";
    public static final String WARNING_EMOTE = "<:LA_warning:954727071014322226>";
    public static final String NOT_FOUND_EMOTE = "<:LA_not_found:954727071026929694>";

    public static final String TWITTER_LOGO_URL = "https://i.imgur.com/QvQPU4a.png";

    public static final long[] TWITTER_USERS = new long[]{
            1291477342624210944L // @playlostark
    };

    public static final long[] TWITTER_USERS_TEST = new long[]{
            1067457730439462912L
    };
}
