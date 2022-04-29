package dev.mayuna.lostarkbot.util.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import dev.mayuna.lostarkbot.util.Constants;
import dev.mayuna.lostarkbot.util.JsonUtils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Deprecated(forRemoval = true)
public class LegacyConfig {

    @Deprecated private static @Getter @Setter String prefix = "la!";
    @Deprecated private static @Getter @Setter String token = "### YOUR TOKEN HERE ###";
    @Deprecated private static @Getter @Setter String unofficialLostArkAPIUrl = "https://lost-ark-api.vercel.app";
    @Deprecated private static @Getter @Setter long exceptionMessageChannelID = 0;
    @Deprecated private static @Getter @Setter long ownerID = 0;
    @Deprecated private static @Getter @Setter boolean debug = false;
    @Deprecated private static @Getter List<String> contributors = new ArrayList<>();
    @Deprecated private static @Getter List<String> westNorthAmerica = new ArrayList<>();
    @Deprecated private static @Getter List<String> eastNorthAmerica = new ArrayList<>();
    @Deprecated private static @Getter List<String> centralEurope = new ArrayList<>();
    @Deprecated private static @Getter List<String> southAmerica = new ArrayList<>();
    @Deprecated private static @Getter List<String> europeWest = new ArrayList<>();
    @Deprecated private static @Getter int totalShards = 1;
    @Deprecated private static @Getter int totalUpdateThreadPool = 2;
    @Deprecated private static @Getter long waitTimeBetweenDashboardUpdates = 50;
    @Deprecated private static @Getter long waitTimeBetweenServerStatusMessages = 50;
    @Deprecated private static @Getter long waitTimeBetweenNotificationMessages = 50;
    @Deprecated private static @Getter long waitTimeBetweenTweets = 50;
    @Deprecated private static @Getter @Setter String twitterAPIKey = "### YOUR TWITTER API KEY HERE ###";
    @Deprecated private static @Getter @Setter String twitterAPIKeySecret = "### YOUR TWITTER API KEY SECRET HERE ###";
    @Deprecated private static @Getter @Setter String twitterAccessToken = "### YOUR TWITTER ACCESS TOKEN HERE ###";
    @Deprecated private static @Getter @Setter String twitterAccessTokenSecret = "### YOUR TWITTER ACCESS TOKEN SECRET HERE ###";
    @Deprecated private static @Getter @Setter boolean twitterTestMode = false;
    @Deprecated private static @Getter @Setter String topggToken = "### YOUR TOP.GG TOKEN HERE ###";
    @Deprecated private static @Getter @Setter boolean updateTopggBotStats = false;
    @Deprecated private static @Getter @Setter long botId = 0;

    public static boolean load() {
        try {
            if (!Files.exists(Path.of(Constants.CONFIG_PATH))) {
                return true;
            }

            Logger.info("Loading legacy config...");
            MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(Constants.CONFIG_PATH);

            if (mayuJson.has("version")) {
                return true;
            }

            prefix = mayuJson.getOrCreate("prefix", new JsonPrimitive(prefix)).getAsString();
            token = mayuJson.getOrCreate("token", new JsonPrimitive(token)).getAsString();
            unofficialLostArkAPIUrl = mayuJson.getOrCreate("unofficialLostArkAPIUrl", new JsonPrimitive(unofficialLostArkAPIUrl)).getAsString();
            exceptionMessageChannelID = mayuJson.getOrCreate("exceptionMessageChannelID", new JsonPrimitive(exceptionMessageChannelID)).getAsLong();
            ownerID = mayuJson.getOrCreate("ownerID", new JsonPrimitive(ownerID)).getAsLong();
            debug = mayuJson.getOrCreate("debug", new JsonPrimitive(debug)).getAsBoolean();
            twitterTestMode = mayuJson.getOrCreate("twitterTestMode", new JsonPrimitive(twitterTestMode)).getAsBoolean();

            contributors = JsonUtils.toStringList(mayuJson.getOrCreate("contributors", new JsonArray()).getAsJsonArray());

            westNorthAmerica = JsonUtils.toStringList(mayuJson.getOrCreate("westNorthAmerica", new JsonArray()).getAsJsonArray());
            eastNorthAmerica = JsonUtils.toStringList(mayuJson.getOrCreate("eastNorthAmerica", new JsonArray()).getAsJsonArray());
            centralEurope = JsonUtils.toStringList(mayuJson.getOrCreate("centralEurope", new JsonArray()).getAsJsonArray());
            southAmerica = JsonUtils.toStringList(mayuJson.getOrCreate("southAmerica", new JsonArray()).getAsJsonArray());
            europeWest = JsonUtils.toStringList(mayuJson.getOrCreate("europeWest", new JsonArray()).getAsJsonArray());

            totalShards = mayuJson.getOrCreate("totalShards", new JsonPrimitive(totalShards)).getAsInt();
            totalUpdateThreadPool = mayuJson.getOrCreate("totalUpdateThreadPool", new JsonPrimitive(totalUpdateThreadPool)).getAsInt();

            waitTimeBetweenDashboardUpdates = mayuJson.getOrCreate("waitTimeBetweenDashboardUpdates", new JsonPrimitive(waitTimeBetweenDashboardUpdates)).getAsLong();
            waitTimeBetweenServerStatusMessages = mayuJson.getOrCreate("waitTimeBetweenServerStatusMessages", new JsonPrimitive(waitTimeBetweenServerStatusMessages)).getAsLong();
            waitTimeBetweenNotificationMessages = mayuJson.getOrCreate("waitTimeBetweenNotificationMessages", new JsonPrimitive(waitTimeBetweenNotificationMessages)).getAsLong();
            waitTimeBetweenTweets = mayuJson.getOrCreate("waitTimeBetweenTweets", new JsonPrimitive(waitTimeBetweenTweets)).getAsLong();

            twitterAPIKey = mayuJson.getOrCreate("twitterAPIKey", new JsonPrimitive(twitterAPIKey)).getAsString();
            twitterAPIKeySecret = mayuJson.getOrCreate("twitterAPIKeySecret", new JsonPrimitive(twitterAPIKeySecret)).getAsString();
            twitterAccessToken = mayuJson.getOrCreate("twitterAccessToken", new JsonPrimitive(twitterAccessToken)).getAsString();
            twitterAccessTokenSecret = mayuJson.getOrCreate("twitterAccessTokenSecret", new JsonPrimitive(twitterAccessTokenSecret)).getAsString();

            topggToken = mayuJson.getOrCreate("topggToken", new JsonPrimitive(topggToken)).getAsString();
            updateTopggBotStats = mayuJson.getOrCreate("updateTopggBotStats", new JsonPrimitive(updateTopggBotStats)).getAsBoolean();
            botId = mayuJson.getOrCreate("botId", new JsonPrimitive(botId)).getAsLong();

            Config.get().loadLegacyConfig();

            return true;
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.error("Error occurred while loading config from path " + Constants.CONFIG_PATH + "!");
            return false;
        }
    }
}
