package dev.mayuna.lostarkbot.objects.other;

import dev.mayuna.lostarkbot.util.Constants;
import dev.mayuna.lostarkbot.util.StringUtils;
import dev.mayuna.mayusjdautils.util.DiscordUtils;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import twitter4j.MediaEntity;
import twitter4j.Status;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MayuTweet {

    private final @Getter Status status;
    private @Getter MessageBuilder messageBuilder;

    public MayuTweet(Status status) {
        this.status = status;
    }

    public String getText() {
        return status.getText();
    }

    public String getFormattedText() {
        return StringUtils.replaceTwitterElements(getText());
    }

    public String getUserName() {
        return status.getUser().getName();
    }

    public String getUserTag() {
        return status.getUser().getScreenName();
    }

    public String getProfileUrl() {
        return "https://twitter.com/%s".formatted(getUserTag());
    }

    public String getProfilePictureUrl() {
        return status.getUser().getMiniProfileImageURL();
    }

    public String getMediaUrl() {
        for (MediaEntity mediaEntity : status.getMediaEntities()) {
            return mediaEntity.getMediaURLHttps();
        }

        return null;
    }

    public String[] getMediaUrls() {
        List<String> urls = new LinkedList<>();

        for (MediaEntity mediaEntity : status.getMediaEntities()) {
            urls.add(mediaEntity.getMediaURLHttps());
        }

        return urls.toArray(new String[0]);
    }

    public boolean hasMedia() {
        return getMediaUrl() != null;
    }

    public boolean hasMoreMedia() {
        return getMediaUrls().length > 1;
    }

    public long getTweetId() {
        return status.getId();
    }

    public String getTweetUrl() {
        return "https://twitter.com/%s/status/%s".formatted(getUserTag(), getTweetId());
    }

    public boolean isReply() {
        return status.getInReplyToStatusId() > 0;
    }

    public boolean isRetweet() {
        return status.isRetweet();
    }

    public boolean isQuoted() {
        return status.getQuotedStatusId() > 0;
    }

    public Status getQuotedStatus() {
        return status.getQuotedStatus();
    }

    public boolean doesMatchKeywords(List<String> keywords) {
        if (keywords.isEmpty()) {
            return true;
        }

        String text = getText().toLowerCase();

        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public boolean doesMatchTwitterUsers(List<String> followedUsers) {
        if (followedUsers.isEmpty()) {
            return false;
        }

        String userTag = getUserTag();

        for (String followedUser : followedUsers) {
            if (userTag.equalsIgnoreCase(followedUser)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Preprocesses fancy tweet message
     */
    public void preProcessMessage() {
        messageBuilder = createTweetMessageBuilder();
    }

    /**
     * Creates fancy tweet message
     * @return {@link MessageBuilder}
     */
    public MessageBuilder createTweetMessageBuilder() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder baseEmbedBuilder = DiscordUtils.getDefaultEmbed();
        List<MessageEmbed> finalEmbeds = new ArrayList<>(4);

        baseEmbedBuilder.setColor(new Color(29, 161, 242));
        baseEmbedBuilder.setFooter("Twitter", Constants.TWITTER_LOGO_URL);

        baseEmbedBuilder.setAuthor(this.getUserName() + " (@" + this.getUserTag() + ")", this.getProfileUrl(), this.getProfilePictureUrl());

        String description = "";
        if (this.isReply()) {
            description = "*Replied*\n";
        } else if (this.isRetweet()) {
            description = "*Retweeted*\n";
        } else if (this.isQuoted()) {
            description = "*Quoted*\n";
        } else {
            description = "*Tweeted*\n";
        }

        description += this.getFormattedText() + "\n\n";

        if (this.isQuoted()) {
            description += "*Quoted tweet*\n";

            MayuTweet quotedMayuTweet = new MayuTweet(this.getQuotedStatus());
            description += "[@" + quotedMayuTweet.getUserTag() + "](" + quotedMayuTweet.getProfileUrl() + "): " + quotedMayuTweet.getFormattedText() + "\n\n";
        }

        description += "[See more](" + this.getTweetUrl() + ")";
        baseEmbedBuilder.setDescription(description);

        if (this.hasMoreMedia()) {
            baseEmbedBuilder.setTitle("\u200E", this.getProfileUrl());

            String[] imageUrls = this.getMediaUrls();
            baseEmbedBuilder.setImage(imageUrls[0]);

            for (int x = 1; x < imageUrls.length; x++) {
                finalEmbeds.add(new EmbedBuilder().setTitle("\u200E", this.getProfileUrl()).setImage(imageUrls[x]).build());
            }
        } else {
            baseEmbedBuilder.setImage(this.getMediaUrl());
        }

        finalEmbeds.add(0, baseEmbedBuilder.build());
        messageBuilder.setEmbeds(finalEmbeds);

        return messageBuilder;
    }
}
