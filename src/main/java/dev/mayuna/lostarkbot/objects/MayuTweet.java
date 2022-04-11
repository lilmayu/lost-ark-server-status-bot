package dev.mayuna.lostarkbot.objects;

import dev.mayuna.lostarkbot.util.EmbedUtils;
import dev.mayuna.lostarkbot.util.StringUtils;
import lombok.Getter;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import twitter4j.MediaEntity;
import twitter4j.Status;

import java.util.LinkedList;
import java.util.List;

public class MayuTweet {

    private final @Getter Status status;
    private @Getter Message message;

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

        String text = status.getText().toLowerCase();
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public void preProcessMessage() {
        message = EmbedUtils.createTweetMessage(this).build();
    }
}
