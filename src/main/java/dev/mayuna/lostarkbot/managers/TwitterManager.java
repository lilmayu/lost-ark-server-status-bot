package dev.mayuna.lostarkbot.managers;

import dev.mayuna.lostarkbot.objects.MayuTweet;
import dev.mayuna.lostarkbot.util.*;
import dev.mayuna.lostarkbot.util.logging.Logger;
import lombok.Getter;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.*;

public class TwitterManager {

    private static @Getter TwitterStream twitterStream;
    private static final FilterQuery filterQuery = new FilterQuery();
    private static long[] followingUsers;

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

        if (!Config.isTwitterTestMode()) {
            followingUsers = Constants.TWITTER_USERS;
        } else {
            followingUsers = Utils.concatenate(Constants.TWITTER_USERS, Constants.TWITTER_USERS_TEST);
        }

        filterQuery.follow(followingUsers);

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

            for (long twitterUserId : followingUsers) {
                if (status.getUser().getId() == twitterUserId) {
                    Logger.debug("[Twitter] Processing status: " + status.getId() + " url: " + mayuTweet.getTweetUrl());

                    processTweet(mayuTweet);
                    return;
                }
            }

            Logger.debug("[Twitter] Ignoring status: " + status.getId() + " url: " + mayuTweet.getTweetUrl() + " - since it is from different user...");
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
