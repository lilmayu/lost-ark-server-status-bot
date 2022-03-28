package dev.mayuna.lostarkbot.objects;

import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsPostObject;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsObject;
import lombok.Getter;

import java.util.*;

public class Notifications {

    private @Getter Map<NewsObject, NewsCategory> news = new HashMap<>();
    private @Getter Map<ForumsPostObject, ForumsCategory> forums = new HashMap<>();

    public Notifications() {
    }

    public boolean isThereAnyUnreadNotification() {
        return !news.isEmpty() || !forums.isEmpty();
    }

    public void add(Notifications notifications) {
        addNewsAll(notifications.getNews());
        addForumsAll(notifications.getForums());
    }

    public void addNews(NewsObject newsObject, NewsCategory newsCategory) {
        this.news.put(newsObject, newsCategory);
    }

    public void addNewsAll(Map<NewsObject, NewsCategory> news) {
        this.news.putAll(news);
    }

    public void addForums(ForumsPostObject forumsPostObject, ForumsCategory forumsCategory) {
        this.forums.put(forumsPostObject, forumsCategory);
    }

    public void addForumsAll(Map<ForumsPostObject, ForumsCategory> forums) {
        this.forums.putAll(forums);
    }

    public List<NewsObject> getNewsByCategory(NewsCategory newsCategory) {
        List<NewsObject> newsObjects = new LinkedList<>();

        for (Map.Entry<NewsObject, NewsCategory> entry : news.entrySet()) {
            if (entry.getValue() == newsCategory) {
                newsObjects.add(entry.getKey());
            }
        }

        return newsObjects;
    }

    public List<ForumsPostObject> getForumsByCategory(ForumsCategory forumsCategory) {
        List<ForumsPostObject> forumsPostObject = new LinkedList<>();

        for (Map.Entry<ForumsPostObject, ForumsCategory> entry : forums.entrySet()) {
            if (entry.getValue() == forumsCategory) {
                forumsPostObject.add(entry.getKey());
            }
        }

        return forumsPostObject;
    }
}
