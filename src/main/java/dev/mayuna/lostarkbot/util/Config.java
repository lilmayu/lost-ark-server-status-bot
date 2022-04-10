package dev.mayuna.lostarkbot.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Config {

    private static @Getter @Setter String prefix = "la!";
    private static @Getter @Setter String token = "### YOUR TOKEN HERE ###";
    private static @Getter @Setter String unofficialLostArkAPIUrl = "https://lost-ark-api.vercel.app";
    private static @Getter @Setter long exceptionMessageChannelID = 0;
    private static @Getter @Setter long ownerID = 0;
    private static @Getter @Setter boolean debug = false;

    private static @Getter List<String> contributors = new ArrayList<>();

    private static @Getter List<String> westNorthAmerica = new ArrayList<>();
    private static @Getter List<String> eastNorthAmerica = new ArrayList<>();
    private static @Getter List<String> centralEurope = new ArrayList<>();
    private static @Getter List<String> southAmerica = new ArrayList<>();
    private static @Getter List<String> europeWest = new ArrayList<>();

    private static @Getter int totalShards = 1;
    private static @Getter int totalUpdateThreadPool = 2;

    private static @Getter long waitTimeBetweenDashboardUpdates = 50;
    private static @Getter long waitTimeBetweenServerStatusMessages = 50;
    private static @Getter long waitTimeBetweenNotificationMessages = 50;
    private static @Getter long waitTimeBetweenTweets = 50;

    private static @Getter @Setter String twitterAPIKey = "### YOUR TWITTER API KEY HERE ###";
    private static @Getter @Setter String twitterAPIKeySecret = "### YOUR TWITTER API KEY SECRET HERE ###";
    private static @Getter @Setter String twitterAccessToken = "### YOUR TWITTER ACCESS TOKEN HERE ###";
    private static @Getter @Setter String twitterAccessTokenSecret = "### YOUR TWITTER ACCESS TOKEN SECRET HERE ###";

    public static boolean load() {
        try {
            MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(Constants.CONFIG_PATH);

            prefix = mayuJson.getOrCreate("prefix", new JsonPrimitive(prefix)).getAsString();
            token = mayuJson.getOrCreate("token", new JsonPrimitive(token)).getAsString();
            unofficialLostArkAPIUrl = mayuJson.getOrCreate("unofficialLostArkAPIUrl", new JsonPrimitive(unofficialLostArkAPIUrl)).getAsString();
            exceptionMessageChannelID = mayuJson.getOrCreate("exceptionMessageChannelID", new JsonPrimitive(exceptionMessageChannelID)).getAsLong();
            ownerID = mayuJson.getOrCreate("ownerID", new JsonPrimitive(ownerID)).getAsLong();
            debug = mayuJson.getOrCreate("debug", new JsonPrimitive(debug)).getAsBoolean();

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

            mayuJson.saveJson();
            return true;
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.error("Error occurred while loading config from path " + Constants.CONFIG_PATH + "!");
            return false;
        }
    }

    public static void save() {
        try {
            MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(Constants.CONFIG_PATH);

            mayuJson.add("prefix", prefix);
            mayuJson.add("token", token);
            mayuJson.add("unofficialLostArkAPIUrl", unofficialLostArkAPIUrl);
            mayuJson.add("exceptionMessageChannelID", exceptionMessageChannelID);
            mayuJson.add("ownerID", ownerID);
            mayuJson.add("debug", debug);

            mayuJson.add("contributors", JsonUtils.toStringJsonArray(contributors));

            mayuJson.add("westNorthAmerica", JsonUtils.toStringJsonArray(westNorthAmerica));
            mayuJson.add("eastNorthAmerica", JsonUtils.toStringJsonArray(eastNorthAmerica));
            mayuJson.add("centralEurope", JsonUtils.toStringJsonArray(centralEurope));
            mayuJson.add("southAmerica", JsonUtils.toStringJsonArray(southAmerica));
            mayuJson.add("europeWest", JsonUtils.toStringJsonArray(europeWest));

            mayuJson.add("totalShards", totalShards);
            mayuJson.add("totalUpdateThreadPool", totalUpdateThreadPool);

            mayuJson.add("waitTimeBetweenDashboardUpdates", waitTimeBetweenDashboardUpdates);
            mayuJson.add("waitTimeBetweenServerStatusMessages", waitTimeBetweenServerStatusMessages);
            mayuJson.add("waitTimeBetweenNotificationMessages", waitTimeBetweenNotificationMessages);
            mayuJson.add("waitTimeBetweenTweets", waitTimeBetweenTweets);

            mayuJson.add("twitterAPIKey", twitterAPIKey);
            mayuJson.add("twitterAPIKeySecret", twitterAPIKeySecret);
            mayuJson.add("twitterAccessToken", twitterAccessToken);
            mayuJson.add("twitterAccessTokenSecret", twitterAccessTokenSecret);

            mayuJson.saveJson();

            Logger.success("Successfully saved config!");
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.error("Error occurred while saving config to path " + Constants.CONFIG_PATH + "!");
        }
    }
}
