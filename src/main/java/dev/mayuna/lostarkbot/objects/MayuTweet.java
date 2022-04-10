package dev.mayuna.lostarkbot.objects;

import lombok.Getter;
import twitter4j.Status;

import java.util.List;

public class MayuTweet {

    private final @Getter Status status;

    public MayuTweet(Status status) {
        this.status = status;
    }

    public long getId() {
        return status.getId();
    }

    public String getUrl() {
        // https://twitter.com/doubutsuno_mori/status/1512989516360998917
        return "https://twitter.com/" + status.getUser().getScreenName() + "/status/" + getId();
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
}
