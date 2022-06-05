package dev.mayuna.lostarkbot.objects.other;

import dev.mayuna.lostarkbot.objects.features.lostark.WrappedForumTopic;
import dev.mayuna.lostarkfetcher.objects.api.LostArkNews;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

public class Notifications {

    private final @Getter List<LostArkNews> news = new LinkedList<>();
    private final @Getter List<WrappedForumTopic> forumTopics = new LinkedList<>();

    public Notifications() {
    }

    public boolean isThereAnyUnreadNotification() {
        return !news.isEmpty() || !forumTopics.isEmpty();
    }

    public void add(Notifications notifications) {
        addNewsAll(notifications.getNews());
        addForumTopicAll(notifications.getForumTopics());
    }

    public void addNews(LostArkNews lostArkNews) {
        this.news.add(lostArkNews);
    }

    public void addNewsAll(List<LostArkNews> news) {
        this.news.addAll(news);
    }

    public void addForumTopic(WrappedForumTopic wrappedForumTopic) {
        this.forumTopics.add(wrappedForumTopic);
    }

    private void addForumTopicAll(List<WrappedForumTopic> forumTopics) {
        this.forumTopics.addAll(forumTopics);
    }

    public List<LostArkNews> getNewsByCategory(StaticNewsTags staticNewsTags) {
        List<LostArkNews> news = new LinkedList<>();

        for (LostArkNews lostArkNews : this.news) {
            if (lostArkNews.getTag().getDisplayName().equalsIgnoreCase(staticNewsTags.getDisplayName())) {
                news.add(lostArkNews);
            }
        }

        return news;
    }

    public List<WrappedForumTopic> getForumTopicsByForumCategoryId(int forumCategoryId) {
        List<WrappedForumTopic> forumTopics = new LinkedList<>();

        for (WrappedForumTopic wrappedForumTopic : this.forumTopics) {
            if (wrappedForumTopic.getForumCategoryId() == forumCategoryId) {
                forumTopics.add(wrappedForumTopic);
            }
        }

        return forumTopics;
    }
}
