package dev.mayuna.lostarkbot.util.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.mayuna.lostarkbot.util.Constants;
import dev.mayuna.lostarkbot.util.logging.Logger;
import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Config {

    private static Config instance = new Config();

    private final @Getter String version = "1.0";
    private @Getter Bot bot = new Bot();
    private @Getter URLs urls = new URLs();
    private @Getter LostArk lostArk = new LostArk();
    private @Getter Twitter twitter = new Twitter();
    private @Getter TopGG topgg = new TopGG();
    private @Getter WaitTimes waitTimes = new WaitTimes();

    private Config() {
    }

    public static boolean load() {
        try {
            if (!Files.exists(getPath())) {
                if (save()) {
                    Logger.info("Config file was created. Bot will now shutdown.");
                    System.exit(0);
                } else {
                    Logger.fatal("Could not save config!");
                }
                return false;
            }

            instance = getGson().fromJson(String.join("", Files.readAllLines(getPath())), Config.class);
            return true;
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.fatal("Could not load config (" + Constants.CONFIG_PATH + ")!");
            return false;
        }
    }

    public void loadLegacyConfig() {
        bot.token = LegacyConfig.getToken();
        bot.exceptionMessageChannelID = LegacyConfig.getExceptionMessageChannelID();
        bot.ownerId = LegacyConfig.getOwnerID();
        bot.botId = LegacyConfig.getBotId();
        bot.totalShards = LegacyConfig.getTotalShards();
        bot.totalUpdateThreads = LegacyConfig.getTotalUpdateThreadPool();
        bot.contributors = LegacyConfig.getContributors();

        urls.unofficialLostArkAPI = LegacyConfig.getUnofficialLostArkAPIUrl();

        lostArk.westNorthAmerica = LegacyConfig.getWestNorthAmerica();
        lostArk.eastNorthAmerica = LegacyConfig.getEastNorthAmerica();
        lostArk.centralEurope = LegacyConfig.getCentralEurope();
        lostArk.southAmerica = LegacyConfig.getSouthAmerica();
        lostArk.europeWest = LegacyConfig.getEuropeWest();

        twitter.APIKey = LegacyConfig.getTwitterAPIKey();
        twitter.APIKeySecret = LegacyConfig.getTwitterAPIKeySecret();
        twitter.accessToken = LegacyConfig.getTwitterAccessToken();
        twitter.accessTokenSecret = LegacyConfig.getTwitterAccessTokenSecret();
        twitter.testMode = LegacyConfig.isTwitterTestMode();

        topgg.topggToken = LegacyConfig.getTopggToken();
        topgg.updateTopggBotStats = LegacyConfig.isUpdateTopggBotStats();

        waitTimes.dashboardUpdates = LegacyConfig.getWaitTimeBetweenDashboardUpdates();
        waitTimes.serverStatusMessages = LegacyConfig.getWaitTimeBetweenServerStatusMessages();
        waitTimes.notificationMessages = LegacyConfig.getWaitTimeBetweenNotificationMessages();
        waitTimes.tweets = LegacyConfig.getWaitTimeBetweenTweets();

        save();
    }

    public static boolean save() {
        try {
            Files.writeString(getPath(), getGson().toJson(instance), StandardOpenOption.CREATE);
            return true;
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.fatal("Could not save config (" + Constants.CONFIG_PATH + ")!");
            return false;
        }
    }

    public static Config get() {
        return instance;
    }

    private static Path getPath() {
        return Path.of(Constants.CONFIG_PATH);
    }

    private static Gson getGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public static class Bot {

        private @Getter String token = "### YOUR TOKEN HERE ###";
        private @Getter long exceptionMessageChannelID = 0;
        private @Getter long ownerId = 0;
        private @Getter long botId = 0;

        private @Getter int totalShards = 1;
        private @Getter int totalUpdateThreads = 2;

        private @Getter List<String> contributors = new ArrayList<>();
    }

    public static class URLs {

        private @Getter String unofficialLostArkAPI = "https://lost-ark-api.vercel.app";
    }

    public static class LostArk {

        private @Getter List<String> westNorthAmerica = new ArrayList<>();
        private @Getter List<String> eastNorthAmerica = new ArrayList<>();
        private @Getter List<String> centralEurope = new ArrayList<>();
        private @Getter List<String> southAmerica = new ArrayList<>();
        private @Getter List<String> europeWest = new ArrayList<>();
    }

    public static class Twitter {

        private @Getter String APIKey = "### YOUR TWITTER API KEY HERE ###";
        private @Getter String APIKeySecret = "### YOUR TWITTER API KEY SECRET HERE ###";
        private @Getter String accessToken = "### YOUR TWITTER ACCESS TOKEN HERE ###";
        private @Getter String accessTokenSecret = "### YOUR TWITTER ACCESS TOKEN SECRET HERE ###";

        private @Getter boolean testMode = false;
    }

    public static class TopGG {

        private @Getter String topggToken = "### YOUR TOP.GG TOKEN HERE ###";
        private @Getter boolean updateTopggBotStats = false;
    }

    public static class WaitTimes {

        private @Getter long dashboardUpdates = 50;
        private @Getter long serverStatusMessages = 50;
        private @Getter long notificationMessages = 50;
        private @Getter long tweets = 50;
    }
}
