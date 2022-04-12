package dev.mayuna.lostarkbot.api.unofficial;

import dev.mayuna.lostarkbot.api.Api;
import dev.mayuna.lostarkbot.api.ApiRestAction;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.util.Config;

public class UnofficialLostArkApi implements Api {

    @Override
    public String getApiEndpoint() {
        return Config.getUnofficialLostArkAPIUrl();
    }

    @Override
    public String getToken() {
        return "";
    }

    public ApiRestAction<News> fetchNews(NewsCategory newsCategory) {
        return new ApiRestAction<>(News.class, new News.Request(newsCategory), this);
    }

    public ApiRestAction<Forums> fetchForumPosts(ForumsCategory forumsCategory) {
        return new ApiRestAction<>(Forums.class, new Forums.Request(forumsCategory), this);
    }
}
