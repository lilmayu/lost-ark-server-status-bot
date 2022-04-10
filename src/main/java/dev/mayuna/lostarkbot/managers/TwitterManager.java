package dev.mayuna.lostarkbot.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.mayuna.lostarkbot.objects.MayuTweet;
import dev.mayuna.lostarkbot.util.*;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import lombok.Getter;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.*;

public class TwitterManager {

    private static @Getter TwitterStream twitterStream;
    private static final FilterQuery filterQuery = new FilterQuery();

    private static @Getter final Timer twitterFetchTimer = new Timer("TwitterWorker");

    public static void initStream() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(false)
                .setOAuthConsumerKey(Config.getTwitterAPIKey())
                .setOAuthConsumerSecret(Config.getTwitterAPIKeySecret())
                .setOAuthAccessToken(Config.getTwitterAccessToken())
                .setOAuthAccessTokenSecret(Config.getTwitterAccessTokenSecret());

        twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
        twitterStream.addListener(new TweetListener());

        filterQuery.follow(Constants.TWITTER_USERS);

        twitterStream.filter(filterQuery);
    }

    public static void processTweet(MayuTweet mayuTweet) {
        ShardExecutorManager.submitForEachShard(UpdateType.TWITTER, shardId -> {
            GuildDataManager.processMayuTweet(shardId, mayuTweet);
        });
    }

    private static class TweetListener implements StatusListener {

        @Override
        public void onStatus(Status status) {
            MayuTweet mayuTweet = new MayuTweet(status);

            Logger.debug("[Twitter] Received status: " + status.getId() + " (" + mayuTweet.getUrl() + ")");

            for (long twitterUserId : Constants.TWITTER_USERS) {
                if (status.getUser().getId() == twitterUserId) {
                    processTweet(mayuTweet);
                    return;
                }
            }

            Logger.debug("[Twitter] Ignoring status: " + status.getId() + " (" + mayuTweet.getUrl() + ") since it is from different user...");
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            Logger.debug("[Twitter] Status deletion notice ID: " + statusDeletionNotice.getStatusId());
        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            Logger.info("[Twitter] Track limitation notice: " + numberOfLimitedStatuses);
        }

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {
            Logger.debug("[Twitter] Scrub geo event userId: " + userId + " upToStatusId: " + upToStatusId);
        }

        @Override
        public void onStallWarning(StallWarning warning) {
            Logger.warn("[Twitter] Stall warning: " + warning);
        }

        @Override
        public void onException(Exception ex) {
            Logger.throwing(ex);
            Logger.warn("Twitter stream encountered an exception!");
        }
    }
}
