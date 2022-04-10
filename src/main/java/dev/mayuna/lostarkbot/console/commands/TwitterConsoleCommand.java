package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.objects.MayuTweet;
import dev.mayuna.lostarkbot.util.Config;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayuslibrary.arguments.ArgumentParser;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterConsoleCommand extends AbstractConsoleCommand {

    public TwitterConsoleCommand() {
        this.name = "twitter";
        this.syntax = "<force-tweet <tweet_id>>";
    }

    @Override
    public CommandResult execute(String arguments) {
        ArgumentParser argumentParser = new ArgumentParser(arguments);

        if (!argumentParser.hasArgumentAtIndex(0)) {
            return CommandResult.INCORRECT_SYNTAX;
        }

        switch (argumentParser.getArgumentAtIndex(0).getValue()) {
            case "force-tweet" -> {
                if (!argumentParser.hasArgumentAtIndex(1)) {
                    return CommandResult.INCORRECT_SYNTAX;
                }

                long tweetId = argumentParser.getArgumentAtIndex(1).getValueAsNumber().longValue();

                ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
                configurationBuilder.setDebugEnabled(true)
                        .setOAuthConsumerKey(Config.getTwitterAPIKey())
                        .setOAuthConsumerSecret(Config.getTwitterAPIKeySecret())
                        .setOAuthAccessToken(Config.getTwitterAccessToken())
                        .setOAuthAccessTokenSecret(Config.getTwitterAccessTokenSecret());
                Twitter twitterClient = new TwitterFactory(configurationBuilder.build()).getInstance();

                try {
                    Status status = twitterClient.showStatus(tweetId);

                    Logger.info("Processing tweet @" + status.getUser().getScreenName() + " - '" + status.getText() + "'...");

                    GuildDataManager.processMayuTweet(new MayuTweet(status));
                } catch (Exception exception) {
                    Logger.throwing(exception);
                    Logger.error("Exception occurred while forcing a tweet!");
                }

                return CommandResult.SUCCESS;
            }
        }


        return CommandResult.INCORRECT_SYNTAX;
    }
}
