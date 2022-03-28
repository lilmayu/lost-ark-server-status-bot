package dev.mayuna.lostarkbot.api.unofficial.objects;

import com.google.gson.annotations.SerializedName;
import dev.mayuna.lostarkbot.objects.abstracts.Hashable;
import dev.mayuna.lostarkbot.util.HashUtils;
import lombok.Getter;

public class ForumsPostObject implements Hashable {

    private @Getter @SerializedName("title") String title;
    private @Getter @SerializedName("post_body") String post_body; // Může být delší
    private @Getter @SerializedName("created_at") String createdAt; // 2022-02-28T19:46:59.218Z
    private @Getter @SerializedName("url") String url;
    private @Getter @SerializedName("author") String author;

    @Override
    public String toString() {
        return "ForumsPostObject{" +
                "title='" + title + '\'' +
                ", post_body='" + post_body + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", url='" + url + '\'' +
                ", author='" + author + '\'' +
                '}';
    }

    @Override
    public String hash() {
        return HashUtils.hashMD5(title + "_" + createdAt);
    }
}
