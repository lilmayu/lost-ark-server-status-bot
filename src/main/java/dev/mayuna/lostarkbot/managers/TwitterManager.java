package dev.mayuna.lostarkbot.managers;

import dev.mayuna.lostarkbot.data.GuildDataManager;
import dev.mayuna.lostarkbot.objects.other.MayuTweet;
import dev.mayuna.lostarkbot.objects.other.TwitterUser;
import dev.mayuna.lostarkbot.util.Constants;
import dev.mayuna.lostarkbot.util.UpdateType;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.config.Config;
import dev.mayuna.lostarkbot.util.logging.Logger;
import lombok.Getter;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Timer;

public class TwitterManager {

    private static final FilterQuery filterQuery = new FilterQuery();
    private static @Getter final Timer twitterFetchTimer = new Timer("TwitterWorker");

    private static @Getter TwitterStream twitterStream;
    private static @Getter TwitterUser[] followingTwitterUsers;

    public static void initStream() {
        Logger.info("[TWITTER] Initializing Twitter stream...");

        Config.Twitter twitterConfig = Config.get().getTwitter();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(false)
                .setOAuthConsumerKey(twitterConfig.getAPIKey())
                .setOAuthConsumerSecret(twitterConfig.getAPIKeySecret())
                .setOAuthAccessToken(twitterConfig.getAccessToken())
                .setOAuthAccessTokenSecret(twitterConfig.getAccessTokenSecret());

        twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
        twitterStream.addListener(new TweetListener());

        if (!twitterConfig.isTestMode()) {
            followingTwitterUsers = Constants.TWITTER_USERS;
        } else {
            Logger.info("[TWITTER] Twitter Test Mode is active! Twitter Users Test will be followed as well.");
            followingTwitterUsers = Utils.concatenate(Constants.TWITTER_USERS, Constants.TWITTER_USERS_TEST);
        }

        filterQuery.follow(getIdsFromTwitterUsers(followingTwitterUsers));

        twitterStream.filter(filterQuery);
    }

    public static void processTweet(MayuTweet mayuTweet) {
        mayuTweet.preProcessMessage();

        ShardExecutorManager.submitForEachShard(UpdateType.TWITTER, shardId -> {
            GuildDataManager.processMayuTweet(shardId, mayuTweet);
        });
    }

    private static class TweetListener implements StatusListener {

        @Override
        public void onStatus(Status status) {
            MayuTweet mayuTweet = new MayuTweet(status);

            for (TwitterUser twitterUser : followingTwitterUsers) {
                if (status.getUser().getId() == twitterUser.id()) {
                    Logger.info("[TWITTER] Processing status: " + status.getId() + " url: " + mayuTweet.getTweetUrl());

                    processTweet(mayuTweet);
                    return;
                }
            }

            Logger.debug("[TWITTER] Ignoring status: " + status.getId() + " url: " + mayuTweet.getTweetUrl() + " - since it is from different user...");
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            Logger.debug("[TWITTER] Status deletion notice id: " + statusDeletionNotice.getStatusId());
        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            Logger.info("[TWITTER] Track limitation notice: " + numberOfLimitedStatuses);
        }

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {
            Logger.debug("[TWITTER] Scrub geo event userId: " + userId + " upToStatusId: " + upToStatusId);
        }

        @Override
        public void onStallWarning(StallWarning warning) {
            Logger.warn("[TWITTER] Stall warning: " + warning);
        }

        @Override
        public void onException(Exception ex) {
            Logger.throwing(ex);
            Logger.warn("Twitter stream encountered an exception!");
        }
    }

    private static long[] getIdsFromTwitterUsers(TwitterUser... twitterUsers) {
        if (twitterUsers == null || twitterUsers.length == 0) {
            Logger.warn("[TWITTER] Supplied twitterUsers is null or empty! (bug)");
            return new long[0];
        }

        long[] ids = new long[twitterUsers.length];
        for (int i = 0; i < twitterUsers.length; i++) {
            ids[i] = twitterUsers[i].id();
        }

        return ids;
    }
}
