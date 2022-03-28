package dev.mayuna.lostarkbot.api.unofficial.objects;

import com.google.gson.annotations.SerializedName;
import dev.mayuna.lostarkbot.objects.abstracts.Hashable;
import dev.mayuna.lostarkbot.util.HashUtils;
import lombok.Getter;

public class NewsObject implements Hashable {

    private @Getter @SerializedName("title") String title;
    private @Getter @SerializedName("description") String description;
    private @Getter @SerializedName("thumbnail") String thumbnailUrl;
    private @Getter @SerializedName("url") String url;
    private @Getter @SerializedName("publishDate") String publishDate;
    private @Getter @SerializedName("excerpt") String excerpt;

    @Override
    public String toString() {
        return "NewsObject{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", url='" + url + '\'' +
                ", publishDate='" + publishDate + '\'' +
                ", excerpt='" + excerpt + '\'' +
                '}';
    }

    @Override
    public String hash() {
        return HashUtils.hashMD5(title + "_" + publishDate);
    }
}
