package dev.mayuna.lostarkbot.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static final String HASHTAG_REPLACEMENT = "[#%s](https://twitter.com/search?q=%23%s)";
    public static final String TAG_REPLACEMENT = "[@%s](https://twitter.com/%s)";

    public static final Pattern PATTERN_HASHTAG = Pattern.compile("\\B#[a-zA-Z_0-9]+\\b");
    public static final Pattern PATTERN_TAG = Pattern.compile("\\B@([a-zA-Z_0-9]+)\\b");

    public static String replaceTwitterElements(String text) {
        Matcher hashtagMatcher = PATTERN_HASHTAG.matcher(text);
        Matcher tagMatcher = PATTERN_TAG.matcher(text);

        while (hashtagMatcher.find()) {
            String hashtagFull = hashtagMatcher.group(0);
            String hashtagName = hashtagFull.substring(1);

            text = text.replace(hashtagFull, replaceHashtag(hashtagName));
        }

        while (tagMatcher.find()) {
            String tagFull = tagMatcher.group(0);
            String tagName = tagFull.substring(1);

            text = text.replace(tagFull, replaceTag(tagName));
        }

        return text;
    }

    public static String replaceHashtag(String hashtagName) {
        return HASHTAG_REPLACEMENT.replace("%s", hashtagName);
    }

    public static String replaceTag(String username) {
        return TAG_REPLACEMENT.replace("%s", username);
    }
}
